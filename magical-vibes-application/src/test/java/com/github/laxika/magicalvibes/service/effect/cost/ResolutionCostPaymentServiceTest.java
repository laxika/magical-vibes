package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.cards.e.ElvishSpiritGuide;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.UnlessAnswer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DepartPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SourceControllerDrawsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GetPoisonCountersCost;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import org.junit.jupiter.api.Nested;
import com.github.laxika.magicalvibes.model.effect.LandDropCost;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesWithTotalPowerCost;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResolutionCostPaymentServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private LifeSupport lifeSupport;
    @Mock private AbilityActivationService abilityActivationService;

    private ResolutionCostPaymentService service;
    private GameData gd;
    private UUID payerId;

    @BeforeEach
    void setUp() {
        payerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", payerId, "Payer");
        gd.playerIds.add(payerId);
        gd.playerManaPools.put(payerId, new ManaPool());
        gd.playerLifeTotals.put(payerId, 5);
        service = new ResolutionCostPaymentService(gameQueryService, lifeSupport);
    }

    @Test
    @DisplayName("0 life can always be paid, even by a player who can't lose life (CR 119.4)")
    void zeroLifeIsAlwaysPayable() {
        assertThat(service.canPayLife(gd, payerId, 0)).isTrue();
    }

    @Test
    @DisplayName("A player who can't lose life can't pay life, though their life total covers it (CR 119.8)")
    void playerWhoCantLoseLifeCantPay() {
        when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(false);

        assertThat(service.canPay(gd, payerId, new PayLifeCost(3), null)).isFalse();
    }

    @Test
    @DisplayName("Life greater than the payer's life total can't be paid (CR 119.4)")
    void lifeAboveTheLifeTotalCantBePaid() {
        when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(true);

        assertThat(service.canPay(gd, payerId, new PayLifeCost(5), null)).isTrue();
        assertThat(service.canPay(gd, payerId, new PayLifeCost(6), null)).isFalse();
    }

    @Test
    @DisplayName("Mana the pool can't cover is not paid at all (CR 118.3)")
    void unpayableManaPaysNothing() {
        gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 1);

        assertThat(service.canPay(gd, payerId, new PayManaCost("{2}"), null)).isFalse();
        assertThat(service.payManaFromPool(gd, payerId, new ManaCost("{2}"), UUID.randomUUID())).isFalse();
        assertThat(gd.playerManaPools.get(payerId).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("Mana the pool covers is paid from it")
    void payableManaIsPaid() {
        gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);

        assertThat(service.canPay(gd, payerId, new PayManaCost("{2}"), null)).isTrue();
        assertThat(service.payManaFromPool(gd, payerId, new ManaCost("{2}"), UUID.randomUUID())).isTrue();
        assertThat(gd.playerManaPools.get(payerId).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An any-of cost can be paid while any one of its alternatives can (CR 118.3)")
    void anyOfCostIsPayableWhileOneAlternativeIs() {
        gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);
        AnyOfCost discardOrPay = AnyOfCost.of(new DiscardRandomCardCost(1), new PayManaCost("{2}"));

        assertThat(service.canPay(gd, payerId, discardOrPay, null)).isTrue();
        assertThat(service.canPay(gd, payerId, AnyOfCost.of(new DiscardRandomCardCost(1), new PayManaCost("{3}")),
                null)).isFalse();
    }

    @Test
    @DisplayName("Exiling a graveyard and discarding a hand can be paid with nothing in them")
    void emptyZoneCostsArePayable() {
        assertThat(service.canPay(gd, payerId, new ExileGraveyardCost(), null)).isTrue();
        assertThat(service.canPay(gd, payerId, new DiscardHandCost(), null)).isTrue();
    }

    @Test
    @DisplayName("A player who can't get poison counters can't pay poison counters (CR 118.3)")
    void poisonNeedsAPlayerWhoCanGetPoisonCounters() {
        when(gameQueryService.canPlayerGetPoisonCounters(gd, payerId)).thenReturn(false);

        assertThat(service.canPay(gd, payerId, new GetPoisonCountersCost(5), null)).isFalse();
    }

    @Nested
    @DisplayName("Whether a payer could possibly pay, so is asked at all")
    class CouldPossiblyPay {

        @BeforeEach
        void withAbilityLookups() {
            ReflectionTestUtils.setField(service, "abilityActivationService", abilityActivationService);
            gd.playerBattlefields.put(payerId, new ArrayList<>());
            gd.playerHands.put(payerId, new ArrayList<>());
        }

        @Test
        @DisplayName("A short pool and no mana source at all is a proof that the mana can't be paid (CR 118.3)")
        void shortPoolWithoutAnySourceCantPay() {
            gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 1);

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{2}"), null)).isFalse();
        }

        @Test
        @DisplayName("A pool that covers the mana can pay it")
        void poolThatCoversTheManaCanPay() {
            gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{2}"), null)).isTrue();
        }

        @Test
        @DisplayName("A land is a mana source, though it couldn't make all of the mana: the payer is asked")
        void anyLandMakesThePayerAsked() {
            Permanent forest = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(payerId).add(forest);
            when(gameQueryService.intrinsicBasicLandManaColors(gd, forest)).thenReturn(Set.of(ManaColor.GREEN));

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{5}"), null)).isTrue();
        }

        @Test
        @DisplayName("A permanent with an activated mana ability, tapped or not, is a mana source")
        void permanentWithAManaAbilityIsASource() {
            Permanent elf = new Permanent(new GrizzlyBears());
            elf.tap();
            gd.playerBattlefields.get(payerId).add(elf);
            when(gameQueryService.intrinsicBasicLandManaColors(gd, elf)).thenReturn(Set.of());
            when(gameQueryService.getOverriddenLandManaColors(gd, elf)).thenReturn(List.of());
            when(abilityActivationService.getEffectiveActivatedAbilities(gd, elf)).thenReturn(List.of(
                    new ActivatedAbility(true, null, List.of(new AwardManaEffect(ManaColor.GREEN)), "{T}: Add {G}.")));

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{1}"), null)).isTrue();
        }

        @Test
        @DisplayName("A mana ability that works from the hand (Elvish Spirit Guide) is a mana source (CR 605.3a)")
        void handManaAbilityIsASource() {
            Card guide = new ElvishSpiritGuide();
            gd.playerHands.get(payerId).add(guide);
            when(abilityActivationService.effectiveHandAbilities(gd, guide, payerId))
                    .thenReturn(guide.getActivatedAbilities(Zone.HAND));

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{1}"), null)).isTrue();
        }

        @Test
        @DisplayName("Mana and life: a payer who can't lose life can't pay it, whatever mana they have (CR 119.8)")
        void manaAndLifeNeedsAPayerWhoCanLoseLife() {
            gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);
            when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(false);

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{2}", null, false, 2), null)).isFalse();
        }

        @Test
        @DisplayName("Mana and life: with the mana in the pool and no mana source, too little life is a proof (CR 119.4)")
        void manaAndLifeWithTooLittleLifeAndNoSource() {
            gd.playerManaPools.get(payerId).add(ManaColor.GREEN, 2);
            when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(true);

            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{2}", null, false, 6), null)).isFalse();
            assertThat(service.couldPossiblyPay(gd, payerId, new PayManaCost("{2}", null, false, 5), null)).isTrue();
        }

        @Test
        @DisplayName("Life alone is judged on the life total and the ability to lose life (CR 119.4, 119.8)")
        void lifeAloneIsJudgedOnLife() {
            when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(true);

            assertThat(service.couldPossiblyPay(gd, payerId, new PayLifeCost(5), null)).isTrue();
            assertThat(service.couldPossiblyPay(gd, payerId, new PayLifeCost(6), null)).isFalse();
        }

        @Test
        @DisplayName("An any-of cost is possible while any alternative is")
        void anyOfIsPossibleWhileAnAlternativeIs() {
            when(gameQueryService.canPlayerLoseLife(gd, payerId)).thenReturn(true);

            assertThat(service.couldPossiblyPay(gd, payerId,
                    AnyOfCost.of(new PayManaCost("{R}{R}"), new PayLifeCost(2)), null)).isTrue();
            assertThat(service.couldPossiblyPay(gd, payerId,
                    AnyOfCost.of(new PayManaCost("{R}{R}"), new PayLifeCost(9)), null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Ransoms chosen as each player answers and paid together after the last answer")
    class PaidTogether {

        private UUID otherId;
        private StackEntry entry;

        @BeforeEach
        void withTwoPlayers() {
            otherId = UUID.randomUUID();
            gd.playerIds.add(otherId);
            gd.playerBattlefields.put(payerId, new ArrayList<>());
            gd.playerBattlefields.put(otherId, new ArrayList<>());
            Card source = new Card();
            source.setName("Desecration Demon");
            entry = com.github.laxika.magicalvibes.testutil.TestStackDoor.triggered(source, otherId, source.getName(),
                    List.of());
        }

        @Test
        @DisplayName("Damage and \"has you draw a card\" are chosen with nothing to pick")
        void damageAndDrawAreChosenWithNothingToPick() {
            ResolutionCostPaymentService.Payment payment = new ResolutionCostPaymentService.Payment(
                    entry.getCard(), otherId, null, entry);
            Player payer = new Player(payerId, "Payer");

            assertThat(service.choose(gd, payer, DamageFromSourceCost.of(4), payment).outcome())
                    .isEqualTo(ResolutionCostPaymentService.Outcome.PAID);
            assertThat(service.choose(gd, payer, new SourceControllerDrawsCost(1), payment).objectIds()).isEmpty();
        }

        @Test
        @DisplayName("A sacrifice with only one possible permanent is chosen unasked (CR 608.2d)")
        void onePossibleSacrificeIsChosenUnasked() {
            Permanent bears = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(payerId).add(bears);
            when(gameQueryService.canEffectCauseSacrifice(gd, payerId, otherId)).thenReturn(true);

            ResolutionCostPaymentService.Choice choice = service.choose(gd, new Player(payerId, "Payer"),
                    new SacrificePermanentCost(null, "creature", false),
                    new ResolutionCostPaymentService.Payment(entry.getCard(), otherId, null, entry));

            assertThat(choice.outcome()).isEqualTo(ResolutionCostPaymentService.Outcome.PAID);
            assertThat(choice.objectIds()).containsExactly(bears.getId());
        }

        @Test
        @DisplayName("Every payer is dealt the damage at once, by one damage step after the last answer")
        void damageIsOneStepForEveryPayer() {
            List<com.github.laxika.magicalvibes.model.effect.CardEffect> steps = service.payTogether(gd, entry, DamageFromSourceCost.of(6), List.of(
                    new UnlessAnswer.Choice(payerId, List.of()), new UnlessAnswer.Choice(otherId, List.of())));

            assertThat(steps).singleElement().isEqualTo(
                    new DealDamageToPlayersEffect(new Fixed(6), Subjects.acceptedPlayers()));
            assertThat(entry.getAcceptedPlayerIds()).containsExactly(payerId, otherId);
        }

        @Test
        @DisplayName("Two players choosing \"has you draw a card\" in one round draw one card")
        void drawIsMadeOnceForTheRound() {
            List<com.github.laxika.magicalvibes.model.effect.CardEffect> steps = service.payTogether(gd, entry, new SourceControllerDrawsCost(1), List.of(
                    new UnlessAnswer.Choice(payerId, List.of()), new UnlessAnswer.Choice(otherId, List.of())));

            assertThat(steps).containsExactly(new DrawCardEffect(1, Subjects.you()));
        }

        @Test
        @DisplayName("The chosen permanents are sacrificed as one departure; one gone by then is skipped")
        void sacrificesAreOneDepartureSkippingWhatLeft() {
            Permanent first = new Permanent(new GrizzlyBears());
            Permanent second = new Permanent(new GrizzlyBears());
            UUID goneId = UUID.randomUUID();
            lenient().when(gameQueryService.findPermanentById(gd, goneId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, first.getId())).thenReturn(first);
            when(gameQueryService.findPermanentById(gd, second.getId())).thenReturn(second);
            when(gameQueryService.findPermanentController(gd, first.getId())).thenReturn(payerId);
            when(gameQueryService.findPermanentController(gd, second.getId())).thenReturn(otherId);

            List<com.github.laxika.magicalvibes.model.effect.CardEffect> steps = service.payTogether(gd, entry, new SacrificePermanentCost(null, "creature", false),
                    List.of(new UnlessAnswer.Choice(payerId, List.of(first.getId(), goneId)),
                            new UnlessAnswer.Choice(otherId, List.of(second.getId()))));

            assertThat(steps).singleElement().isEqualTo(new DepartPermanentsEffect(List.of(
                    DepartureIntent.sacrifice(first.getId()), DepartureIntent.sacrifice(second.getId()))));
        }

        @Test
        @DisplayName("A payer whose opponents' effects can't make them sacrifice (Sigarda) can't pay a sacrifice")
        void sigardaKeepsThePayerFromPayingASacrifice() {
            gd.playerBattlefields.get(payerId).add(new Permanent(new GrizzlyBears()));
            when(gameQueryService.canEffectCauseSacrifice(gd, payerId, otherId)).thenReturn(false);

            assertThat(service.canPay(gd, payerId, new SacrificePermanentCost(null, "creature", false), null, entry))
                    .isFalse();
        }

        @Test
        @DisplayName("A permanent that can't be sacrificed doesn't pay a sacrifice")
        void permanentThatCantBeSacrificedDoesNotPay() {
            Permanent bears = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(payerId).add(bears);
            when(gameQueryService.canEffectCauseSacrifice(gd, payerId, otherId)).thenReturn(true);
            when(gameQueryService.cantBeChosenToSacrifice(eq(gd), eq(bears), any())).thenReturn(true);

            assertThat(service.canPay(gd, payerId, new SacrificePermanentCost(null, "creature", false), null, entry))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("Returning, putting on top, giving up a land drop and sacrificing for total power")
    class OtherRansoms {

        private PredicateEvaluationService predicates;

        @BeforeEach
        void withPredicates() {
            predicates = org.mockito.Mockito.mock(PredicateEvaluationService.class);
            ReflectionTestUtils.setField(service, "predicateEvaluationService", predicates);
            gd.playerBattlefields.put(payerId, new ArrayList<>());
        }

        @Test
        @DisplayName("{D} can be paid only on the payer's own turn with a land drop left, and paying uses one up")
        void landDropIsPayableOnlyWithADropLeft() {
            gd.activePlayerId = payerId;
            when(gameQueryService.getMaxLandsThisTurn(gd, payerId)).thenReturn(1);

            assertThat(service.canPay(gd, payerId, new LandDropCost(), null)).isTrue();
            assertThat(service.pay(gd, new Player(payerId, "Payer"), new LandDropCost(),
                    new ResolutionCostPaymentService.Payment(new GrizzlyBears(), payerId, null)))
                    .isEqualTo(ResolutionCostPaymentService.Outcome.PAID);
            assertThat(gd.landsPlayedThisTurn.get(payerId)).isOne();
            assertThat(service.canPay(gd, payerId, new LandDropCost(), null)).isFalse();

            gd.activePlayerId = UUID.randomUUID();
            assertThat(service.canPay(gd, payerId, new LandDropCost(), null)).isFalse();
        }

        @Test
        @DisplayName("A card can be put on top of the library only from a hand with a card in it")
        void putOnTopNeedsACardInHand() {
            gd.playerHands.put(payerId, new ArrayList<>());
            assertThat(service.canPay(gd, payerId, new PutCardFromHandOnTopOfLibraryCost(), null)).isFalse();

            gd.playerHands.get(payerId).add(new GrizzlyBears());
            assertThat(service.canPay(gd, payerId, new PutCardFromHandOnTopOfLibraryCost(), null)).isTrue();
        }

        @Test
        @DisplayName("A return is paid with a permanent the payer controls, any player's only when the cost says so")
        void returnIsTheirOwnUnlessAnyPlayers() {
            UUID otherId = UUID.randomUUID();
            Permanent theirs = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.put(otherId, new ArrayList<>(List.of(theirs)));
            when(predicates.matchesPermanentPredicate(eq(gd), eq(theirs), any())).thenReturn(true);

            assertThat(service.canPay(gd, payerId, ReturnPermanentToHandCost.youControl(
                    new PermanentTruePredicate(), "creature you control"), null)).isFalse();
            assertThat(service.canPay(gd, payerId, ReturnPermanentToHandCost.anyPlayers(
                    new PermanentTruePredicate(), "creature"), null)).isTrue();
        }

        @Test
        @DisplayName("Creatures with total power 12 can be sacrificed only when their positive powers reach 12")
        void totalPowerCountsOnlyPositivePower() {
            Permanent big = new Permanent(new GrizzlyBears());
            Permanent shrunk = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(payerId).addAll(List.of(big, shrunk));
            when(predicates.matchesPermanentPredicate(eq(gd), any(Permanent.class), any())).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, big.getId())).thenReturn(big);
            when(gameQueryService.findPermanentById(gd, shrunk.getId())).thenReturn(shrunk);
            when(gameQueryService.getEffectivePower(gd, big)).thenReturn(11);
            when(gameQueryService.getEffectivePower(gd, shrunk)).thenReturn(-2);

            assertThat(service.canPay(gd, payerId, new SacrificeCreaturesWithTotalPowerCost(12), null)).isFalse();

            when(gameQueryService.getEffectivePower(gd, big)).thenReturn(12);
            assertThat(service.canPay(gd, payerId, new SacrificeCreaturesWithTotalPowerCost(12), null)).isTrue();
        }

        @Test
        @DisplayName("Mana paid once for each of an amount must be read before it is paid")
        void manaPerAmountIsReadFirst() {
            assertThatThrownBy(() -> ResolutionCostPaymentService.manaCostOf(
                    PayManaCost.perAmount("{G}", new CountersOnSource(CounterType.WIND))))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("Discarding three cards needs three cards in hand and asks for all three at once")
    void discardingThreeCardsAsksForThree() {
        PlayerInputService playerInputService = org.mockito.Mockito.mock(PlayerInputService.class);
        ReflectionTestUtils.setField(service, "playerInputService", playerInputService);
        DiscardCardTypeCost discardThree = new DiscardCardTypeCost(null, null, 3);
        gd.playerHands.put(payerId, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        assertThat(service.canPay(gd, payerId, discardThree, null)).isFalse();

        gd.playerHands.get(payerId).add(new GrizzlyBears());
        assertThat(service.pay(gd, new Player(payerId, "Payer"), discardThree,
                new ResolutionCostPaymentService.Payment(new GrizzlyBears(), payerId, null)))
                .isEqualTo(ResolutionCostPaymentService.Outcome.ASKING);
        verify(playerInputService).beginDiscardChoice(eq(gd), eq(payerId), eq(List.of(0, 1, 2)),
                eq("Choose 3 cards to discard."), eq(3));
    }
}
