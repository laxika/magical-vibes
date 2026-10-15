package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DragonsPrey;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IridescentTiger;
import com.github.laxika.magicalvibes.cards.k.KinTreeNurturer;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AleshasLegacy.class, IridescentTiger.class})
class AleshasLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Grants deathtouch and indestructible to a creature you control")
    void grantsKeywordsToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        castAleshasLegacy(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Keyword grants expire at end of turn")
    void keywordGrantsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        castAleshasLegacy(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IridescentTiger());
        harness.setHand(player1, List.of(new AleshasLegacy()));
        addManaForAleshasLegacy();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Only the targeted creature gains the keywords")
    void onlyTargetGainsKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new IridescentTiger());

        castAleshasLegacy(target);

        for (Permanent unaffected : List.of(other, opponent)) {
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.DEATHTOUCH)).isFalse();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.INDESTRUCTIBLE)).isFalse();
        }
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AleshasLegacy()));
        addManaForAleshasLegacy();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @CardUsed(KinTreeNurturer.class)
    @DisplayName("Deathtouch kills a larger blocker while indestructible prevents lethal combat damage")
    void keywordsWorkInCombatAndDamageClearsBeforeTheyExpire() {
        Permanent attacker = addCreatureReady(player1, new KinTreeNurturer());
        Permanent blocker = addCreatureReady(player2, new IridescentTiger());
        castAleshasLegacy(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Iridescent Tiger");
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed(DragonsPrey.class)
    @DisplayName("Indestructible prevents a destroy effect")
    void preventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        castAleshasLegacy(target);
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Dragon's Prey");
    }

    @Test
    @CardUsed(DragonsPrey.class)
    @DisplayName("Alesha's Legacy does not resolve when its target is destroyed in response")
    void targetDestroyedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new IridescentTiger());
        harness.setHand(player1, List.of(new AleshasLegacy(), new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(other);
        harness.assertInGraveyard(player1, "Alesha's Legacy");
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castAleshasLegacy(Permanent target) {
        harness.setHand(player1, List.of(new AleshasLegacy()));
        addManaForAleshasLegacy();

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addManaForAleshasLegacy() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
