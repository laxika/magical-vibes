package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OltecMatterweaver.class, GrizzlyBears.class, Spellbook.class})
class OltecMatterweaverTest extends BaseCardTest {

    private static final String GNOME_MODE = "Create a 1/1 colorless Gnome artifact creature token";
    private static final String COPY_MODE = "Create a token that's a copy of target artifact token you control";

    @Test
    @DisplayName("Creates a colorless artifact Gnome token when its creature-cast trigger chooses that mode")
    void createsGnomeToken() {
        addMatterweaver();

        castCreatureAndChooseGnome();

        Permanent gnome = findPermanent(player1, "Gnome");
        assertThat(gnome.getCard().isToken()).isTrue();
        assertThat(gnome.getCard().getColors()).isEmpty();
        assertThat(gnome.getEffectivePower()).isEqualTo(1);
        assertThat(gnome.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a copy of a target artifact token it controls")
    void copiesTargetArtifactToken() {
        addMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");
        Permanent matterweaver = findPermanent(player1, "Oltec Matterweaver");

        castCreatureSpell();
        harness.passBothPriorities();
        harness.handleListChoice(player1, COPY_MODE);
        harness.handlePermanentChosen(player1, gnome.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(2);
        assertThat(findPermanents(player1, "Oltec Matterweaver")).containsExactly(matterweaver);
    }

    @Test
    @DisplayName("Only artifact tokens controlled by the controller are legal copy targets")
    void copyModeRestrictsTargets() {
        addMatterweaver();
        castCreatureAndChooseGnome();
        Permanent gnome = findPermanent(player1, "Gnome");
        Permanent matterweaver = findPermanent(player1, "Oltec Matterweaver");
        Permanent opponentMatterweaver = addCreatureReady(player2, new OltecMatterweaver());

        castCreatureSpell();
        harness.passBothPriorities();
        harness.handleListChoice(player1, COPY_MODE);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(gnome.getId())
                .doesNotContain(matterweaver.getId(), opponentMatterweaver.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, matterweaver.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature spell is cast")
    void doesNotTriggerForNoncreatureSpell() {
        addMatterweaver();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Gnome")).isEmpty();
    }

    private Permanent addMatterweaver() {
        return addCreatureReady(player1, new OltecMatterweaver());
    }

    private void castCreatureAndChooseGnome() {
        castCreatureSpell();
        harness.passBothPriorities();
        harness.handleListChoice(player1, GNOME_MODE);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
