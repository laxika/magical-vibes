package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostLitRaider;
import com.github.laxika.magicalvibes.cards.e.Excruciator;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SekkiSeasonsGuide.class, GhostLitRaider.class, GhostLitRedeemer.class, HandOfHonor.class,
        Skullcrack.class, Excruciator.class})
class SekkiSeasonsGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with eight +1/+1 counters")
    void entersWithEightCounters() {
        harness.castFromHand(player1, new SekkiSeasonsGuide(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        Permanent sekki = findPermanent(player1, "Sekki, Seasons' Guide");
        assertThat(sekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Prevents damage, removes matching counters, and creates colorless Spirit tokens")
    void preventsDamageRemovesCountersAndCreatesTokens() {
        Permanent sekki = harness.enterBattlefieldAndReturn(player2, new SekkiSeasonsGuide());

        dealTwoDamageTo(sekki);

        Permanent survivingSekki = findPermanent(player2, "Sekki, Seasons' Guide");
        assertThat(survivingSekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(spiritTokens(player2)).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getColors()).isEmpty();
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(survivingSekki.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creates tokens for all prevented damage even when counters run out")
    void createsTokensForDamageBeyondCounterCount() {
        Permanent sekki = harness.addToBattlefieldAndReturn(player2, new SekkiSeasonsGuide());
        sekki.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        dealTwoDamageTo(sekki);

        harness.assertNotOnBattlefield(player2, "Sekki, Seasons' Guide");
        assertThat(spiritTokens(player2)).hasSize(2);
        harness.assertInGraveyard(player2, "Sekki, Seasons' Guide");
    }

    @Test
    @DisplayName("Sacrificing eight Spirits returns Sekki from the graveyard")
    void sacrificesEightSpiritsToReturnFromGraveyard() {
        SekkiSeasonsGuide sekki = new SekkiSeasonsGuide();
        harness.setGraveyard(player1, List.of(sekki));

        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new GhostLitRedeemer());
        }

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(sekki.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Ghost-Lit Redeemer"));
        Permanent returnedSekki = findPermanent(player1, "Sekki, Seasons' Guide");
        assertThat(returnedSekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Sacrificing eight Spirits leaves non-Spirit permanents untouched")
    void sacrificeAbilityOnlySacrificesSpirits() {
        SekkiSeasonsGuide sekki = new SekkiSeasonsGuide();
        harness.setGraveyard(player1, List.of(sekki));

        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new GhostLitRedeemer());
        }
        harness.addToBattlefield(player1, new HandOfHonor());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sekki, Seasons' Guide");
        harness.assertOnBattlefield(player1, "Hand of Honor");
        harness.assertNotOnBattlefield(player1, "Ghost-Lit Redeemer");
    }

    @Test
    @CardUsed(Skullcrack.class)
    @DisplayName("Unpreventable damage still removes counters and creates Spirits")
    void unpreventableDamageStillCreatesSpirits() {
        Permanent sekki = harness.enterBattlefieldAndReturn(player2, new SekkiSeasonsGuide());
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        dealTwoDamageTo(sekki);

        assertThat(sekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(sekki.getMarkedDamage()).isEqualTo(2);
        assertThat(spiritTokens(player2)).hasSize(2);
    }

    @Test
    @DisplayName("Opponent's Spirits cannot pay the graveyard ability cost")
    void cannotActivateWithOnlySevenControlledSpirits() {
        harness.setGraveyard(player1, List.of(new SekkiSeasonsGuide()));
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new GhostLitRedeemer());
        }
        harness.addToBattlefield(player2, new GhostLitRedeemer());
        harness.addToBattlefield(player1, new HandOfHonor());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Sekki, Seasons' Guide");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(8);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spirits are sacrificed as a cost before Sekki returns")
    void sacrificesArePaidBeforeResolution() {
        harness.setGraveyard(player1, List.of(new SekkiSeasonsGuide()));
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new GhostLitRedeemer());
        }

        harness.activateGraveyardAbility(player1, 0);

        harness.assertInGraveyard(player1, "Sekki, Seasons' Guide");
        harness.assertNotOnBattlefield(player1, "Sekki, Seasons' Guide");
        harness.assertNotOnBattlefield(player1, "Ghost-Lit Redeemer");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sekki, Seasons' Guide");
        harness.assertNotInGraveyard(player1, "Sekki, Seasons' Guide");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("An older activation cannot return Sekki after it returns and dies again")
    void olderActivationCannotReturnNewGraveyardObject() {
        harness.setGraveyard(player2, List.of(new SekkiSeasonsGuide()));
        for (int activation = 0; activation < 2; activation++) {
            for (int i = 0; i < 8; i++) {
                harness.addToBattlefield(player2, new GhostLitRedeemer());
            }
            harness.activateGraveyardAbility(player2, 0);
        }
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        Permanent sekki = findPermanent(player2, "Sekki, Seasons' Guide");
        assertThat(gd.stack).hasSize(1);

        for (int i = 0; i < 4; i++) {
            dealTwoDamageTo(sekki);
        }
        harness.assertInGraveyard(player2, "Sekki, Seasons' Guide");
        harness.assertNotOnBattlefield(player2, "Sekki, Seasons' Guide");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sekki, Seasons' Guide");
        harness.assertNotOnBattlefield(player2, "Sekki, Seasons' Guide");
    }

    @Test
    @CardUsed(Excruciator.class)
    @DisplayName("Unpreventable combat damage still applies Sekki's counter removal and token creation")
    void unpreventableCombatDamageStillAppliesAdditionalEffects() {
        addCreatureReady(player1, new Excruciator());
        harness.enterBattlefieldAndReturn(player2, new SekkiSeasonsGuide());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(spiritTokens(player2)).hasSize(7);
        harness.assertInGraveyard(player2, "Sekki, Seasons' Guide");
        harness.assertNotOnBattlefield(player2, "Sekki, Seasons' Guide");
    }

    private void dealTwoDamageTo(Permanent target) {
        Permanent raider = addCreatureReady(player1, new GhostLitRaider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(raider);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private List<Permanent> spiritTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .toList();
    }
}
