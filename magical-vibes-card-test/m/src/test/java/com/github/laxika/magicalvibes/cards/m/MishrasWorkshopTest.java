package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgivianArchaeologist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasWorkshop.class, Millstone.class, ArgivianArchaeologist.class})
class MishrasWorkshopTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mishra's Workshop adds three artifact-spell-only colorless mana")
    void tappingProducesRestrictedMana() {
        harness.addToBattlefield(player1, new MishrasWorkshop());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyColorless()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The restricted mana pays for an artifact spell")
    void restrictedManaPaysForArtifactSpell() {
        harness.addToBattlefield(player1, new MishrasWorkshop());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new Millstone()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("The restricted mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifactSpell() {
        harness.addToBattlefield(player1, new MishrasWorkshop());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new ArgivianArchaeologist()));

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restricted mana cannot pay for an artifact activated ability")
    void restrictedManaCannotPayForArtifactAbility() {
        harness.addToBattlefield(player1, new MishrasWorkshop());
        harness.addToBattlefield(player1, new Millstone());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyColorless()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyColorless()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The mana ability resolves immediately and cannot be activated again while tapped")
    void manaAbilityRequiresUntappedWorkshop() {
        harness.addToBattlefield(player1, new MishrasWorkshop());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyColorless()).isEqualTo(3);
    }
}
