package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfTerribleSecrets;
import com.github.laxika.magicalvibes.cards.k.KamisFlare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouAreAlreadyDead.class, Forest.class, KamiOfTerribleSecrets.class, KamisFlare.class})
class YouAreAlreadyDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a damaged creature and draws a card")
    void destroysDamagedCreatureAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KamiOfTerribleSecrets());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kami of Terrible Secrets");
        harness.assertInGraveyard(player2, "Kami of Terrible Secrets");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KamiOfTerribleSecrets());
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Draws a card when destruction is prevented")
    void drawsWhenDestructionIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KamiOfTerribleSecrets());
        target.setRegenerationShield(1);
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kami of Terrible Secrets");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Can destroy a creature dealt noncombat damage by a spell")
    void destroysCreatureDamagedBySpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KamiOfTerribleSecrets());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new KamisFlare(), new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Kami of Terrible Secrets");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Kami of Terrible Secrets");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw when the only target leaves the battlefield")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KamiOfTerribleSecrets());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "You Are Already Dead");
    }

    @Test
    @DisplayName("Cannot target a damaged noncreature permanent")
    void cannotTargetDamagedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its controller's damaged creature")
    void destroysOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KamiOfTerribleSecrets());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new YouAreAlreadyDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Kami of Terrible Secrets");
        harness.assertInHand(player1, "Forest");
    }
}
