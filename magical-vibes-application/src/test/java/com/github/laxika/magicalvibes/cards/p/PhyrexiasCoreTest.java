package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexiasCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new PhyrexiasCore());

        Permanent core = findPermanent(player1, "Phyrexia's Core");
        core.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability gains 1 life on resolution")
    void sacrificeArtifactGainsLife() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        Permanent core = findPermanent(player1, "Phyrexia's Core");
        core.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent core = findPermanent(player1, "Phyrexia's Core");
        core.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent core = findPermanent(player1, "Phyrexia's Core");
        core.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple artifacts, asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new PhyrexiasCore());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent core = findPermanent(player1, "Phyrexia's Core");
        core.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

}
