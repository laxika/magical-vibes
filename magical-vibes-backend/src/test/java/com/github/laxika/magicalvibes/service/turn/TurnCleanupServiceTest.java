package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TurnCleanupServiceTest {

    @Mock
    private AuraAttachmentService auraAttachmentService;

    @InjectMocks
    private TurnCleanupService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
    }

    private static Card createCardWithName(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    @Nested
    @DisplayName("applyCleanupResets")
    class ApplyCleanupResets {

        @Test
        @DisplayName("Resets end-of-turn modifiers and returns stolen creatures")
        void delegatesToResetAndReturnStolen() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.applyCleanupResets(gd);

            assertThat(perm.getPowerModifier()).isZero();
            verify(auraAttachmentService).returnStolenCreatures(gd, true);
        }
    }

    @Nested
    @DisplayName("resetEndOfTurnModifiers")
    class ResetEndOfTurnModifiers {

        @Test
        @DisplayName("Clears power and toughness modifiers on permanents")
        void clearsPowerToughnessModifiers() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(3);
            perm.setToughnessModifier(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }

        @Test
        @DisplayName("Clears damage prevention and regeneration shields on permanents")
        void clearsDamagePreventionAndRegenerationShields() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setDamagePreventionShield(5);
            perm.setRegenerationShield(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getDamagePreventionShield()).isZero();
            assertThat(perm.getRegenerationShield()).isZero();
        }

        @Test
        @DisplayName("Clears marked damage on all permanents (CR 514.2)")
        void clearsMarkedDamage() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(4);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Clears granted keywords on permanents")
        void clearsGrantedKeywords() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.getGrantedKeywords().add(Keyword.FLYING);
            perm.getGrantedKeywords().add(Keyword.TRAMPLE);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getGrantedKeywords()).isEmpty();
        }

        @Test
        @DisplayName("Clears cantBeBlocked flag on permanents")
        void clearsCantBeBlocked() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setCantBeBlocked(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isCantBeBlocked()).isFalse();
        }

        @Test
        @DisplayName("Clears animatedUntilEndOfTurn flag on permanents")
        void clearsAnimatedUntilEndOfTurn() {
            Card card = createCardWithName("Mutavault");
            Permanent perm = new Permanent(card);
            perm.setAnimatedUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isAnimatedUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears cantRegenerateThisTurn flag on permanents")
        void clearsCantRegenerateThisTurn() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setCantRegenerateThisTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isCantRegenerateThisTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears exileInsteadOfDieThisTurn flag on permanents")
        void clearsExileInsteadOfDieThisTurn() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setExileInsteadOfDieThisTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isExileInsteadOfDieThisTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears granted card types on permanents")
        void clearsGrantedCardTypes() {
            Card card = createCardWithName("Mutavault");
            Permanent perm = new Permanent(card);
            perm.getGrantedCardTypes().add(CardType.CREATURE);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getGrantedCardTypes()).isEmpty();
        }

        @Test
        @DisplayName("Clears mustAttackThisTurn flag on permanents")
        void clearsMustAttackThisTurn() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setMustAttackThisTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isMustAttackThisTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears basePowerToughnessOverriddenUntilEndOfTurn flag on permanents")
        void clearsBasePowerToughnessOverridden() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears losesAllAbilitiesUntilEndOfTurn flag on permanents")
        void clearsLosesAllAbilitiesUntilEndOfTurn() {
            Card card = createCardWithName("Alloy Myr");
            Permanent perm = new Permanent(card);
            perm.setLosesAllAbilitiesUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isLosesAllAbilitiesUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears losesAllAbilitiesUntilEndOfTurn even when no other modifiers are set")
        void clearsLosesAllAbilitiesAlone() {
            Card card = createCardWithName("Alloy Myr");
            Permanent perm = new Permanent(card);
            // Only set losesAllAbilities — no power/toughness/keyword modifiers
            perm.setLosesAllAbilitiesUntilEndOfTurn(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.isLosesAllAbilitiesUntilEndOfTurn()).isFalse();
        }

        @Test
        @DisplayName("Clears global damage prevention flags")
        void clearsGlobalDamagePreventionFlags() {
            gd.globalDamagePreventionShield = 10;
            gd.preventAllCombatDamage = true;
            gd.allPermanentsEnterTappedThisTurn = true;

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.globalDamagePreventionShield).isZero();
            assertThat(gd.preventAllCombatDamage).isFalse();
            assertThat(gd.allPermanentsEnterTappedThisTurn).isFalse();
        }

        @Test
        @DisplayName("Clears player damage prevention shields")
        void clearsPlayerDamagePreventionShields() {
            gd.playerDamagePreventionShields.put(player1Id, 5);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerDamagePreventionShields).isEmpty();
        }

        @Test
        @DisplayName("Clears damage redirect shields")
        void clearsDamageRedirectShields() {
            Card card = createCardWithName("Vengeful Archon");
            gd.damageRedirectShields.add(new DamageRedirectShield(
                    player1Id, 3, UUID.randomUUID(), card, player2Id));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.damageRedirectShields).isEmpty();
        }

        @Test
        @DisplayName("Clears source damage redirect shields")
        void clearsSourceDamageRedirectShields() {
            gd.sourceDamageRedirectShields.add(new SourceDamageRedirectShield(
                    player1Id, UUID.randomUUID(), 2, player2Id));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.sourceDamageRedirectShields).isEmpty();
        }

        @Test
        @DisplayName("Clears preventDamageFromColors")
        void clearsPreventDamageFromColors() {
            gd.preventDamageFromColors.add(CardColor.RED);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.preventDamageFromColors).isEmpty();
        }

        @Test
        @DisplayName("Clears combatDamageRedirectTarget")
        void clearsCombatDamageRedirectTarget() {
            gd.combatDamageRedirectTarget = player2Id;

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.combatDamageRedirectTarget).isNull();
        }

        @Test
        @DisplayName("Clears combatDamageExemptPredicate")
        void clearsCombatDamageExemptPredicate() {
            gd.combatDamageExemptPredicate = new com.github.laxika.magicalvibes.model.filter.PermanentPredicate() {};

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.combatDamageExemptPredicate).isNull();
        }

        @Test
        @DisplayName("Clears playerColorDamagePreventionCount")
        void clearsPlayerColorDamagePreventionCount() {
            gd.playerColorDamagePreventionCount.put(player1Id,
                    new java.util.concurrent.ConcurrentHashMap<>(java.util.Map.of(CardColor.RED, 2)));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerColorDamagePreventionCount).isEmpty();
        }

        @Test
        @DisplayName("Clears playerSourceDamagePreventionIds")
        void clearsPlayerSourceDamagePreventionIds() {
            gd.playerSourceDamagePreventionIds.put(player1Id, Set.of(UUID.randomUUID()));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerSourceDamagePreventionIds).isEmpty();
        }

        @Test
        @DisplayName("Clears permanentsPreventedFromDealingDamage")
        void clearsPermanentsPreventedFromDealingDamage() {
            gd.permanentsPreventedFromDealingDamage.add(UUID.randomUUID());

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
        }

        @Test
        @DisplayName("Clears playersWithAllDamagePrevented")
        void clearsPlayersWithAllDamagePrevented() {
            gd.playersWithAllDamagePrevented.add(player1Id);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playersWithAllDamagePrevented).isEmpty();
        }

        @Test
        @DisplayName("Clears drawReplacementTargetToController")
        void clearsDrawReplacementTargetToController() {
            gd.drawReplacementTargetToController.put(player1Id, player2Id);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.drawReplacementTargetToController).isEmpty();
        }

        @Test
        @DisplayName("Clears playerSpellsCantBeCounteredByColorsThisTurn")
        void clearsSpellsCantBeCountered() {
            gd.playerSpellsCantBeCounteredByColorsThisTurn.put(player1Id, Set.of(CardColor.GREEN));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerSpellsCantBeCounteredByColorsThisTurn).isEmpty();
        }

        @Test
        @DisplayName("Clears playerCreaturesCantBeTargetedByColorsThisTurn")
        void clearsCreaturesCantBeTargeted() {
            gd.playerCreaturesCantBeTargetedByColorsThisTurn.put(player1Id, Set.of(CardColor.BLACK));

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playerCreaturesCantBeTargetedByColorsThisTurn).isEmpty();
        }

        @Test
        @DisplayName("Clears playersSilencedThisTurn")
        void clearsPlayersSilencedThisTurn() {
            gd.playersSilencedThisTurn.add(player1Id);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(gd.playersSilencedThisTurn).isEmpty();
        }

        @Test
        @DisplayName("Does not affect permanents without modifiers")
        void doesNotAffectUnmodifiedPermanents() {
            Card card = createCardWithName("Grizzly Bears");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.resetEndOfTurnModifiers(gd);

            assertThat(perm.getPowerModifier()).isZero();
            assertThat(perm.getToughnessModifier()).isZero();
        }
    }

    @Nested
    @DisplayName("drainManaPools")
    class DrainManaPools {

        @Test
        @DisplayName("Empties all players' mana pools")
        void emptiesAllManaPools() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 3);
            gd.playerManaPools.get(player2Id).add(ManaColor.BLUE, 2);

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isZero();
            assertThat(gd.playerManaPools.get(player2Id).getTotal()).isZero();
        }

        @Test
        @DisplayName("Does not drain mana pools when PreventManaDrainEffect is on the battlefield")
        void doesNotDrainWhenPreventManaDrainPresent() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 5);
            Card card = createCardWithName("Upwelling");
            card.addEffect(EffectSlot.STATIC, new PreventManaDrainEffect());
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(5);
        }

        @Test
        @DisplayName("Does nothing when mana pools are already empty")
        void doesNothingWhenAlreadyEmpty() {
            sut.drainManaPools(gd);

            assertThat(gd.playerManaPools.get(player1Id).getTotal()).isZero();
        }
    }

    @Nested
    @DisplayName("getMaxHandSize")
    class GetMaxHandSize {

        @Test
        @DisplayName("Returns 7 by default")
        void returnsSevenByDefault() {
            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Reduces hand size when opponent controls ReduceOpponentMaxHandSizeEffect")
        void reducedByOpponentReduceEffect() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(7));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(0);
        }

        @Test
        @DisplayName("Does not reduce own controller's hand size")
        void doesNotReduceOwnHandSize() {
            Card card = createCardWithName("Jin-Gitaxias, Core Augur");
            card.addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(7));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(7);
        }

        @Test
        @DisplayName("Multiple reduction effects from the same opponent stack additively")
        void multipleReductionEffectsStack() {
            Card card1 = createCardWithName("Reducer A");
            card1.addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(2));
            Card card2 = createCardWithName("Reducer B");
            card2.addEffect(EffectSlot.STATIC, new ReduceOpponentMaxHandSizeEffect(3));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card2));

            assertThat(sut.getMaxHandSize(gd, player1Id)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("hasNoMaximumHandSize")
    class HasNoMaximumHandSize {

        @Test
        @DisplayName("Returns false by default")
        void returnsFalseByDefault() {
            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns true when player controls NoMaximumHandSizeEffect")
        void returnsTrueWithNoMaxHandSizeEffect() {
            Card card = createCardWithName("Spellbook");
            card.addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Returns true when player is in playersWithNoMaximumHandSize set")
        void returnsTrueWhenInGlobalSet() {
            gd.playersWithNoMaximumHandSize.add(player1Id);

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Opponent's NoMaximumHandSizeEffect does not affect this player")
        void opponentEffectDoesNotAffect() {
            Card card = createCardWithName("Spellbook");
            card.addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.hasNoMaximumHandSize(gd, player1Id)).isFalse();
        }
    }
}
