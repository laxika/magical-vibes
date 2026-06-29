package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OxiddaDaredevilTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability grants haste to Oxidda Daredevil on resolution")
    void grantsHasteOnResolution() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent daredevil = findPermanent(player1, "Oxidda Daredevil");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("Ability can be activated even when summoning sick (no tap cost)")
    void canActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent daredevil = findPermanent(player1, "Oxidda Daredevil");
        assertThat(daredevil.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrificed artifact goes to graveyard")
    void sacrificedArtifactGoesToGraveyard() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

}
