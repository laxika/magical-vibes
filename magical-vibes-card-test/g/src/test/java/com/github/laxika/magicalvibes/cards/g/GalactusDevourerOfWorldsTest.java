package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.cards.s.SilverSurferGalactussHerald;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalactusDevourerOfWorlds.class, SilverSurferGalactussHerald.class, OneWithTheStars.class})
class GalactusDevourerOfWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("When Galactus enters, it exiles target permanent")
    void etbExilesTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SilverSurferGalactussHerald());
        castGalactus(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Galactus must attack each combat when able")
    void mustAttackEachCombat() {
        Permanent galactus = addCreatureReady(player1, new GalactusDevourerOfWorlds());
        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(galactus.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Galactus may stay back while its controller controls Silver Surfer")
    void silverSurferRemovesAttackRequirement() {
        Permanent galactus = addCreatureReady(player1, new GalactusDevourerOfWorlds());
        harness.addToBattlefield(player1, new SilverSurferGalactussHerald());

        declareAttackers(List.of());

        assertThat(galactus.isAttacking()).isFalse();
    }

    private void castGalactus(Permanent target) {
        harness.setHand(player1, List.of(new GalactusDevourerOfWorlds()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void noncreatureSilverSurferDoesNotRemoveAttackRequirement() {
        addCreatureReady(player1, new GalactusDevourerOfWorlds());
        Permanent surfer = harness.addToBattlefieldAndReturn(player1, new SilverSurferGalactussHerald());
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, surfer.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, surfer)).isFalse();
        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void opponentsSilverSurferDoesNotRemoveAttackRequirement() {
        addCreatureReady(player1, new GalactusDevourerOfWorlds());
        harness.addToBattlefield(player2, new SilverSurferGalactussHerald());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedGalactusIsNotRequiredToAttack() {
        Permanent galactus = addCreatureReady(player1, new GalactusDevourerOfWorlds());
        galactus.tap();

        declareAttackers(List.of());

        assertThat(galactus.isAttacking()).isFalse();
    }

    @Test
    void etbCanExileOwnNoncreaturePermanent() {
        Permanent surfer = harness.addToBattlefieldAndReturn(player1, new SilverSurferGalactussHerald());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OneWithTheStars());
        aura.setAttachedTo(surfer.getId());

        castGalactus(aura);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
    }

    @Test
    void etbCanExileGalactusItselfDespiteIndestructible() {
        Permanent galactus = harness.enterBattlefieldAndReturn(player1, new GalactusDevourerOfWorlds());
        harness.handlePermanentChosen(player1, galactus.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(galactus);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(galactus.getCard());
    }
}
