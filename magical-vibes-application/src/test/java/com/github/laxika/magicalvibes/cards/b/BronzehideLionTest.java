package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzehideLion.class, DoomBlade.class, GrizzlyBears.class})
class BronzehideLionTest extends BaseCardTest {

    @Test
    @DisplayName("Bronzehide Lion gains indestructible until end of turn")
    void gainsIndestructible() {
        Permanent lion = addCreatureReady(player1, new BronzehideLion());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lion, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("When Bronzehide Lion dies, it returns as an Aura attached to a creature you control")
    void returnsAsAuraAttachedToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BronzehideLion());

        destroyLion();

        Permanent aura = findPermanent(player1, "Bronzehide Lion");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(aura.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
    }

    @Test
    @DisplayName("The returned Aura grants indestructible to its enchanted creature")
    void returnedAuraGrantsIndestructible() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BronzehideLion());
        destroyLion();

        Permanent aura = findPermanent(player1, "Bronzehide Lion");
        addManaForAbility();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("When multiple creatures are available, Bronzehide Lion's controller chooses the Aura's creature")
    void choosesCreatureForAura() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosenCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BronzehideLion());

        destroyLion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(firstCreature.getId(), chosenCreature.getId());
        harness.handlePermanentChosen(player1, chosenCreature.getId());

        Permanent aura = findPermanent(player1, "Bronzehide Lion");
        assertThat(aura.getAttachedTo()).isEqualTo(chosenCreature.getId());
    }

    @Test
    @DisplayName("Bronzehide Lion remains in its graveyard when no creature can be enchanted")
    void staysInGraveyardWithoutCreature() {
        addCreatureReady(player1, new BronzehideLion());

        destroyLion();

        harness.assertInGraveyard(player1, "Bronzehide Lion");
        harness.assertNotOnBattlefield(player1, "Bronzehide Lion");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void destroyLion() {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Bronzehide Lion"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
