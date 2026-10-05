package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CaptainOfTheMists;
import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.b.BelovedPrincess;
import com.github.laxika.magicalvibes.cards.c.ClaimTheFirstborn;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.s.SyrKonradTheGrim;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({OpportunisticDragon.class, CaptainOfTheMists.class, MindStone.class,
        Unsummon.class, BakeIntoAPie.class, BelovedPrincess.class, ClaimTheFirstborn.class,
        Frogify.class, Gingerbrute.class, GoldenEgg.class, SyrKonradTheGrim.class})
class OpportunisticDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB steals an opposing Human and suppresses its abilities and combat")
    void stealsHumanAndSuppressesIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainOfTheMists());
        target.setSummoningSick(false);

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("The stolen artifact and restrictions return when Opportunistic Dragon leaves")
    void effectsEndWhenDragonLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");
        bounceDragon(dragon);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("If Opportunistic Dragon leaves before its trigger resolves, nothing happens")
    void triggerDoesNothingIfDragonLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainOfTheMists());

        castDragon(target.getId());
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");
        bounceDragon(dragon);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addToBattlefield(player2, new CaptainOfTheMists());
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact creature loses its keywords, activated abilities, and combat permissions")
    void stealsArtifactCreatureAndSuppressesItsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatThrownBy(() -> harness.activateAbility(player1, targetIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A stolen permanent can still untap normally")
    void stolenPermanentUntapsNormally() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        target.setTapped(true);

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Removing the Dragon's abilities does not end its effects")
    void effectsPersistWhenDragonLosesAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BelovedPrincess());
        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");

        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, dragon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, dragon)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();

        bounceDragon(dragon);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("A later control effect does not restore the kidnapped permanent's abilities or combat")
    void restrictionsPersistWhenTargetChangesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BelovedPrincess());
        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ClaimTheFirstborn()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("A target leaving before resolution is not kidnapped")
    void targetLeavingBeforeResolutionIsNotAffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BelovedPrincess());
        castDragon(target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Beloved Princess");
        harness.assertNotOnBattlefield(player1, "Beloved Princess");
        harness.assertOnBattlefield(player1, "Opportunistic Dragon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Dragon can resolve with no legal targets for its triggered ability")
    void resolvesWithoutLegalTargets() {
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Opportunistic Dragon");
        harness.assertOnBattlefield(player1, "Golden Egg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing non-Human nonartifact creature is not a legal target")
    void cannotTargetNonHumanNonartifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OpportunisticDragon());
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The kidnapped Human does not trigger from the Dragon's death")
    void kidnappedHumanDoesNotTriggerWhenDragonDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SyrKonradTheGrim());
        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new BakeIntoAPie()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, dragon.getId());

        harness.assertInGraveyard(player1, "Opportunistic Dragon");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasLostAllAbilities(gd, target)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castDragon(UUID targetId) {
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void bounceDragon(Permanent dragon) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
    }
}
