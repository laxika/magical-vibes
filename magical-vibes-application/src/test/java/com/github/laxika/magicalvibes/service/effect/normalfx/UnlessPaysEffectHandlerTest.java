package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnlessPaysEffectHandlerTest {

    @Mock private SubjectResolutionService subjectResolutionService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private LifeSupport lifeSupport;

    private UnlessPaysEffectHandler handler;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private StackEntry spell;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerManaPools.put(player1Id, new ManaPool());
        Card elves = new Card();
        elves.setName("Llanowar Elves");
        elves.setType(CardType.CREATURE);
        spell = new StackEntry(StackEntryType.CREATURE_SPELL, elves, player1Id, elves.getName(), List.of());
        gd.stack.add(spell);
        lenient().when(subjectResolutionService.spellOnStack(any(), any(), any())).thenReturn(spell);
        handler = new UnlessPaysEffectHandler(subjectResolutionService, amountEvaluationService, gameQueryService,
                gameLogService, lifeSupport);
    }

    private StackEntry counterEntry(UnlessPaysEffect effect) {
        Card hatchling = new Card();
        hatchling.setName("Spiketail Hatchling");
        return TestStackDoor.triggered(hatchling, player2Id, hatchling.getName(), List.of(effect), 0,
                spell.getCard().getId(), null);
    }

    private void payerIsTheSpellsController(StackEntry entry, UnlessPaysEffect effect) {
        when(subjectResolutionService.players(gd, entry, effect, effect.payer())).thenReturn(List.of(player1Id));
    }

    @Test
    @DisplayName("Asks the spell's controller, naming the cost and the spell, when they can pay")
    void asksThePayerWhoCanPay() {
        gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        assertThat(gd.resolvingMayEffectFromStack).isTrue();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
        assertThat(ability.controllerId()).isEqualTo(player1Id);
        assertThat(ability.targetCardId()).isEqualTo(spell.getCard().getId());
        assertThat(ability.manaCost()).isEqualTo("{1}");
        assertThat(ability.description()).isEqualTo("Pay {1} to prevent Llanowar Elves from being countered?");
        assertThat(ability.effects()).containsExactly(effect);
        assertThat(ability.sourceControllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("A payer without the mana can't pay, so isn't asked")
    void payerWithoutTheManaCantPay() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isTrue();
    }

    @Test
    @DisplayName("Nothing is asked once the spell to counter left the stack (countered \"instead\" by an earlier instruction)")
    void nothingAskedOnceTheSpellLeft() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        when(subjectResolutionService.spellOnStack(gd, entry, Subjects.targetSpell()))
                .thenReturn(null);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("A life ransom asks for the life, with no mana on the prompt")
    void lifeRansomAsksForLife() {
        gd.playerLifeTotals.put(player1Id, 20);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysLife(3);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);
        when(amountEvaluationService.evaluate(eq(gd), any(), any())).thenReturn(3);
        when(gameQueryService.canPlayerLoseLife(gd, player1Id)).thenReturn(true);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
        assertThat(ability.manaCost()).isNull();
        assertThat(ability.additionalLifeCost()).isEqualTo(3);
        assertThat(ability.description()).isEqualTo("Pay 3 life to prevent Llanowar Elves from being countered?");
    }

    @Test
    @DisplayName("A player who can't lose life can't pay a life ransom (CR 119.8)")
    void playerWhoCantLoseLifeCantPayLife() {
        gd.playerLifeTotals.put(player1Id, 20);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysLife(3);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);
        when(amountEvaluationService.evaluate(eq(gd), any(), any())).thenReturn(3);
        when(gameQueryService.canPlayerLoseLife(gd, player1Id)).thenReturn(false);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isTrue();
    }

    @Test
    @DisplayName("An {X} ransom is the X announced for the countering spell or ability")
    void xRansomReadsTheAnnouncedX() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysX();
        StackEntry entry = counterEntry(effect);
        entry.setXValue(2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, entry, effect);

        assertThat(ransom.manaCost()).isEqualTo("{2}");
        assertThat(ransom.xCost()).isTrue();
        assertThat(ransom.text()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("A dynamic ransom reads the source as it last existed once it was sacrificed")
    void dynamicRansomReadsTheSourceSnapshot() {
        Card refrain = new Card();
        refrain.setName("Lilting Refrain");
        Permanent source = new Permanent(refrain);
        source.setCounterCount(CounterType.VERSE, 2);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(new CountersOnSource(CounterType.VERSE));
        StackEntry entry = counterEntry(effect);
        entry.setSourcePermanentSnapshot(source);
        when(amountEvaluationService.evaluate(eq(gd), eq(new CountersOnSource(CounterType.VERSE)), any()))
                .thenReturn(2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, entry, effect);

        assertThat(ransom.manaCost()).isEqualTo("{2}");
        verify(amountEvaluationService).evaluate(eq(gd), eq(new CountersOnSource(CounterType.VERSE)),
                argThat(context -> context.sourcePermanent() == source));
    }

    @Test
    @DisplayName("Mana and life together read as one cost")
    void manaAndLifeReadAsOneCost() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(2, 2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, counterEntry(effect), effect);

        assertThat(ransom.text()).isEqualTo("{2} and 2 life");
    }

    @Test
    @DisplayName("Every player the payer subject names is asked in APNAP order, the next one even after one paid (CR 101.4)")
    void everyPayerIsAskedEvenAfterOnePaid() {
        gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 2);
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.playerManaPools.get(player2Id).add(ManaColor.COLORLESS, 2);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        UnlessPaysEffect effect = UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", new DrawCardEffect(1));
        StackEntry entry = counterEntry(effect);
        entry.setResolvingEffectIndex(0);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer()))
                .thenReturn(List.of(player1Id, player2Id));
        gd.pendingEffectResolutionEntry = entry;
        gd.pendingEffectResolutionIndex = 0;

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        PendingMayAbility first = gd.pendingMayAbilities.removeFirst();
        assertThat(first.controllerId()).isEqualTo(player1Id);
        handler.answer(gd, new Player(player1Id, "Player1"), true, first, effect);

        assertThat(gd.playerManaPools.get(player1Id).getTotal()).isZero();
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        PendingMayAbility second = gd.pendingMayAbilities.removeFirst();
        assertThat(second.controllerId()).isEqualTo(player2Id);
        handler.answer(gd, new Player(player2Id, "Player2"), false, second, effect);

        assertThat(gd.playerManaPools.get(player2Id).getTotal()).isEqualTo(2);
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.PAID);
        assertThat(entry.unlessAnswer(0)).isNull();
    }

    @Test
    @DisplayName("A payer with no way at all to pay is left out of the loop; the others are still asked")
    void payerWithNoWayToPayIsLeftOut() {
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.playerManaPools.get(player2Id).add(ManaColor.COLORLESS, 2);
        UnlessPaysEffect effect = UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", new DrawCardEffect(1));
        StackEntry entry = counterEntry(effect);
        entry.setResolvingEffectIndex(0);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer()))
                .thenReturn(List.of(player1Id, player2Id));

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).singleElement()
                .extracting(PendingMayAbility::controllerId).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("With no payer able to pay at all, nobody is asked and the effect's \"unless\" happens")
    void nobodyAbleToPayTakesTheUnpaidBranch() {
        gd.playerManaPools.put(player2Id, new ManaPool());
        UnlessPaysEffect effect = UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", new DrawCardEffect(1));
        StackEntry entry = counterEntry(effect);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer()))
                .thenReturn(List.of(player1Id, player2Id));

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.NOT_PAID);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("An all-of ransom reads as one cost, its parts joined in Oracle order")
    void allOfRansomReadsAsOneCost() {
        UnlessPaysEffect effect = UnlessPaysEffect.youMayPay(
                AllOfCost.of(new PayManaCost("{1}"), new DiscardCardTypeCost(null, null)), new DrawCardEffect(1));

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, counterEntry(effect), effect);

        assertThat(ransom.action()).isEqualTo("Pay {1} and discard a card");
        assertThat(ransom.manaCost()).isEqualTo("{1}");
    }

    @Test
    @DisplayName("Paid together: each answer only chooses, and after the last one the damage comes as one step (CR 101.4)")
    void paidTogetherChoosesThenPaysOnceAfterTheLastAnswer() {
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        UnlessPaysEffect effect = UnlessPaysEffect.mayPay(Subjects.eachPlayer(), DamageFromSourceCost.of(4),
                new SacrificeSelfEffect()).payingTogether();
        StackEntry entry = counterEntry(effect);
        entry.setResolvingEffectIndex(0);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer()))
                .thenReturn(List.of(player1Id, player2Id));
        when(amountEvaluationService.evaluate(eq(gd), eq(new com.github.laxika.magicalvibes.model.amount.Fixed(4)),
                any())).thenReturn(4);
        gd.pendingEffectResolutionEntry = entry;
        gd.pendingEffectResolutionIndex = 0;

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        handler.answer(gd, new Player(player1Id, "Player1"), true, gd.pendingMayAbilities.removeFirst(), effect);
        assertThat(UnlessPaysEffectHandler.anotherPlayerAlreadyPaid(gd)).isTrue();
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        handler.answer(gd, new Player(player2Id, "Player2"), true, gd.pendingMayAbilities.removeFirst(), effect);

        assertThat(entry.getAcceptedPlayerIds()).isEmpty();
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.PAID);
        assertThat(handler.paymentMadeTogether(gd, entry, effect, 0)).singleElement()
                .isEqualTo(new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers()));
        assertThat(entry.getAcceptedPlayerIds()).containsExactly(player1Id, player2Id);
        assertThat(entry.unlessAnswer(0)).isNull();
    }

    @Test
    @DisplayName("Paid together: with every player declining nothing is paid and \"if no one does\" follows")
    void paidTogetherWithEveryoneDecliningPaysNothing() {
        gd.playerIdToName.put(player1Id, "Player1");
        UnlessPaysEffect effect = UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), DamageFromSourceCost.of(4),
                new DrawCardEffect(1)).payingTogether();
        StackEntry entry = counterEntry(effect);
        entry.setResolvingEffectIndex(0);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer())).thenReturn(List.of(player1Id));
        when(amountEvaluationService.evaluate(eq(gd), eq(new com.github.laxika.magicalvibes.model.amount.Fixed(4)),
                any())).thenReturn(4);
        gd.pendingEffectResolutionEntry = entry;
        gd.pendingEffectResolutionIndex = 0;

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        handler.answer(gd, new Player(player1Id, "Player1"), false, gd.pendingMayAbilities.removeFirst(), effect);

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.NOT_PAID);
        assertThat(handler.paymentMadeTogether(gd, entry, effect, 0)).isEmpty();
    }

    @Test
    @DisplayName("Only a sacrifice, tap, damage or draw ransom can be paid together")
    void onlyChoosableRansomsArePaidTogether() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> UnlessPaysEffect.youMayPay("{1}",
                new DrawCardEffect(1)).payingTogether()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Each payer's own branch: after the last answer the players who chose to pay are the accepted players (CR 101.4)")
    void eachUnlessRecordsWhoPaid() {
        gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 2);
        gd.playerManaPools.put(player2Id, new ManaPool());
        gd.playerManaPools.get(player2Id).add(ManaColor.COLORLESS, 2);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        UnlessPaysEffect effect = UnlessPaysEffect.eachUnless(Subjects.eachPlayer(), new PayManaCost("{2}"),
                players -> new com.github.laxika.magicalvibes.model.effect.LoseLifeEffect(1, players));
        StackEntry entry = counterEntry(effect);
        entry.setResolvingEffectIndex(0);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer()))
                .thenReturn(List.of(player1Id, player2Id));
        gd.pendingEffectResolutionEntry = entry;
        gd.pendingEffectResolutionIndex = 0;

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        handler.answer(gd, new Player(player1Id, "Player1"), true, gd.pendingMayAbilities.removeFirst(), effect);
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.ASK);
        handler.resolve(gd, entry, effect);
        handler.answer(gd, new Player(player2Id, "Player2"), false, gd.pendingMayAbilities.removeFirst(), effect);

        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.PAID);
        assertThat(entry.getAcceptedPlayerIds()).containsExactly(player1Id);
        assertThat(effect.ifPaid()).isEqualTo(new com.github.laxika.magicalvibes.model.effect.LoseLifeEffect(1,
                Subjects.except(Subjects.eachPlayer(), Subjects.acceptedPlayers())));
        assertThat(effect.ifNotPaid()).isEqualTo(
                new com.github.laxika.magicalvibes.model.effect.LoseLifeEffect(1, Subjects.eachPlayer()));
    }

    @Test
    @DisplayName("\"{G} for each wind counter\" reads as that many {G}, and as {0} with none")
    void manaPerAmountReadsAsThatManySymbols() {
        UnlessPaysEffect effect = UnlessPaysEffect.unlessYouPay(
                PayManaCost.perAmount("{G}", new CountersOnSource(CounterType.WIND)), new SacrificeSelfEffect());
        StackEntry entry = counterEntry(effect);
        when(amountEvaluationService.evaluate(eq(gd), eq(new CountersOnSource(CounterType.WIND)), any()))
                .thenReturn(3, 0);

        assertThat(handler.ransom(gd, entry, effect).manaCost()).isEqualTo("{G}{G}{G}");
        assertThat(handler.ransom(gd, entry, effect).manaCost()).isEqualTo("{0}");
    }

    @Test
    @DisplayName("The return, put-on-top, land-drop and total-power ransoms ask in their own words")
    void otherRansomsAskInTheirOwnWords() {
        StackEntry entry = counterEntry(UnlessPaysEffect.counterUnlessPays(1));
        java.util.function.Function<com.github.laxika.magicalvibes.model.effect.CostEffect, String> action =
                cost -> handler.ransom(gd, entry, UnlessPaysEffect.unlessYouPay(cost, new SacrificeSelfEffect()))
                        .action();

        assertThat(action.apply(com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost.youControl(
                new com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate(),
                "artifact you control"))).isEqualTo("Return an artifact you control to its owner's hand");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost()))
                .isEqualTo("Put a card from your hand on top of your library");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.LandDropCost())).isEqualTo("Pay {D}");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesWithTotalPowerCost(12)))
                .isEqualTo("Sacrifice creatures with total power 12 or greater");
    }

    @Test
    @DisplayName("A payer that is the effect's target declares it when no branch targets (\"unless target opponent pays\")")
    void payerTargetIsDeclaredWhenNoBranchTargets() {
        UnlessPaysEffect effect = UnlessPaysEffect.unlessPaysLife(Subjects.targetPlayer(), 3, new DrawCardEffect(1));

        assertThat(effect.targetSpec()).isEqualTo(com.github.laxika.magicalvibes.model.effect.TargetSpec.harmful(
                com.github.laxika.magicalvibes.model.effect.TargetPredicates.player()));
        assertThat(UnlessPaysEffect.unlessPaysLife(Subjects.you(), 3, new DrawCardEffect(1)).targetSpec())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE);
    }

    private StackEntry upkeepEntry(UnlessPaysEffect effect, Permanent source) {
        return TestStackDoor.triggered(source.getCard(), player1Id, source.getCard().getName(), List.of(effect), 0,
                source.getId());
    }

    private static Permanent permanentCosting(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost(manaCost);
        return new Permanent(card);
    }

    @Test
    @DisplayName("\"Unless you pay its mana cost\" reads the source permanent's mana cost, an {X} in it being 0")
    void itsManaCostReadsTheSourcesManaCost() {
        UnlessPaysEffect effect = UnlessPaysEffect.unlessYouPay(PayManaCost.forSourceManaCost(),
                new SacrificeSelfEffect());
        Permanent ornithopter = permanentCosting("Endless One", "{X}{G}");
        StackEntry entry = upkeepEntry(effect, ornithopter);
        when(gameQueryService.findPermanentById(gd, ornithopter.getId())).thenReturn(ornithopter);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, entry, effect);

        assertThat(ransom.unpayable()).isFalse();
        assertThat(ransom.manaCost()).isEqualTo("{0}{G}");
    }

    @Test
    @DisplayName("\"Its mana cost\" of a source that left reads the source as it last existed (CR 608.2h)")
    void itsManaCostReadsTheSourceAsItLastExisted() {
        UnlessPaysEffect effect = UnlessPaysEffect.unlessYouPay(PayManaCost.forSourceManaCost(),
                new SacrificeSelfEffect());
        Permanent bauble = permanentCosting("Mox Opal", "{2}");
        StackEntry entry = upkeepEntry(effect, bauble);
        entry.setSourcePermanentSnapshot(bauble);

        assertThat(handler.ransom(gd, entry, effect).manaCost()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("A source with no mana cost, or a face-down one, has an unpayable cost: the payer isn't asked (CR 118.6)")
    void noManaCostIsUnpayableAndNotAsked() {
        UnlessPaysEffect effect = UnlessPaysEffect.unlessYouPay(PayManaCost.forSourceManaCost(),
                new SacrificeSelfEffect());
        Permanent token = permanentCosting("Construct", null);
        StackEntry entry = upkeepEntry(effect, token);
        when(gameQueryService.findPermanentById(gd, token.getId())).thenReturn(token);
        when(subjectResolutionService.players(gd, entry, effect, effect.payer())).thenReturn(List.of(player1Id));

        assertThat(handler.ransom(gd, entry, effect).unpayable()).isTrue();
        assertThat(handler.nextStep(gd, entry, effect, 0)).isEqualTo(UnlessPaysEffectHandler.Step.NOT_PAID);

        Permanent faceDown = permanentCosting("Krosan Cloudscraper", "{7}{G}{G}{G}");
        faceDown.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        StackEntry faceDownEntry = upkeepEntry(effect, faceDown);
        when(gameQueryService.findPermanentById(gd, faceDown.getId())).thenReturn(faceDown);
        assertThat(handler.ransom(gd, faceDownEntry, effect).unpayable()).isTrue();
    }

    @Test
    @DisplayName("A life-only ransom, \"another\" and a counted sacrifice ask in the Oracle text's words")
    void lifeAnotherAndCountedSacrificesAskInTheirWords() {
        StackEntry entry = counterEntry(UnlessPaysEffect.counterUnlessPays(1));
        java.util.function.Function<UnlessPaysEffect, String> action = effect -> handler.ransom(gd, entry, effect)
                .action();
        when(amountEvaluationService.evaluate(eq(gd), eq(new com.github.laxika.magicalvibes.model.amount.Fixed(1)), any()))
                .thenReturn(1);

        assertThat(action.apply(UnlessPaysEffect.unlessYouPayLife(1, new SacrificeSelfEffect()))).isEqualTo("Pay 1 life");
        assertThat(action.apply(UnlessPaysEffect.unlessYouPay(new com.github.laxika.magicalvibes.model.effect
                .SacrificePermanentCost(new com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate(),
                "another creature"), new SacrificeSelfEffect()))).isEqualTo("Sacrifice another creature");
        assertThat(action.apply(UnlessPaysEffect.unlessYouPay(new com.github.laxika.magicalvibes.model.effect
                .SacrificeMultiplePermanentsCost(2, new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate(),
                "Swamps"), new SacrificeSelfEffect()))).isEqualTo("Sacrifice two Swamps");
    }

    @Test
    @DisplayName("The energy, mill, graveyard and counter ransoms ask in the Oracle text's words")
    void forcedCostRansomsAskInTheirWords() {
        StackEntry entry = counterEntry(UnlessPaysEffect.counterUnlessPays(1));
        java.util.function.Function<com.github.laxika.magicalvibes.model.effect.CostEffect, String> action =
                cost -> handler.ransom(gd, entry, UnlessPaysEffect.unlessYouPay(cost, new SacrificeSelfEffect()))
                        .action();
        when(amountEvaluationService.evaluate(eq(gd), eq(new com.github.laxika.magicalvibes.model.amount.Fixed(2)), any()))
                .thenReturn(2);

        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.PayEnergyCost(2)))
                .isEqualTo("Pay {E}{E}");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.MillControllerCost(2)))
                .isEqualTo("Mill two cards");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost(
                com.github.laxika.magicalvibes.model.CardType.CREATURE)))
                .isEqualTo("Exile the top creature card of your graveyard");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost(2)))
                .isEqualTo("Put two cards from your graveyard on the bottom of your library");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost()))
                .isEqualTo("Remove a counter from a permanent you control");
        assertThat(action.apply(new com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost(
                List.of(com.github.laxika.magicalvibes.model.CounterType.QUEST), 3, null, false)))
                .isEqualTo("Remove three quest counters from among permanents you control");
        assertThat(action.apply(com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost.youControl(2,
                new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate(), "Forests you control")))
                .isEqualTo("Return two Forests you control to their owner's hand");
    }

    @Test
    @DisplayName("Echo's cost is asked as the cost it names, an {X} read as it resolves, and a \"rather than pay the "
            + "echo cost\" {0} is offered first, the echo cost after it")
    void echoCostIsAskedAsTheCostItNames() {
        StackEntry entry = counterEntry(UnlessPaysEffect.counterUnlessPays(1));
        java.util.function.Function<com.github.laxika.magicalvibes.model.effect.PayEchoCost, List<String>> actions =
                echo -> handler.alternatives(gd, entry, UnlessPaysEffect.unlessYouPay(echo, new SacrificeSelfEffect()))
                        .stream().map(UnlessPaysEffectHandler.Ransom::action).toList();
        com.github.laxika.magicalvibes.model.amount.DynamicAmount life =
                new com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal();
        when(amountEvaluationService.evaluate(eq(gd), eq(life), any())).thenReturn(17);

        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost("{1}{G}")))
                .containsExactly("Pay {1}{G}");
        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost(null, null, null, life)))
                .containsExactly("Pay {17}");
        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost(
                new com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost(null, null))))
                .containsExactly("Discard a card");
        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost(
                new com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost(2,
                        new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate(), "lands"))))
                .containsExactly("Sacrifice two lands");

        when(gameQueryService.findAlternativeEchoCost(gd, player2Id)).thenReturn("{0}");
        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost(
                new com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost(null, null))))
                .containsExactly("Pay {0}", "Discard a card");
        assertThat(actions.apply(new com.github.laxika.magicalvibes.model.effect.PayEchoCost("{0}")))
                .containsExactly("Pay {0}");
    }
}
