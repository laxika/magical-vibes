package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringjackPastureTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds {C}")
    void firstAbilityAddsColorless() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        int idx = indexOf(pasture);

        harness.activateAbility(player1, idx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a 0/1 white Goat token for {4}")
    void secondAbilityCreatesGoat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent pasture = addPasture();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int idx = indexOf(pasture);
        harness.activateAbility(player1, idx, 1, null, null);
        harness.passBothPriorities();

        Permanent goat = findPermanent(player1, "Goat");
        assertThat(goat.getCard().getPower()).isEqualTo(0);
        assertThat(goat.getCard().getToughness()).isEqualTo(1);
        assertThat(goat.getCard().getSubtypes()).contains(CardSubtype.GOAT);
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability: sacrifice 2 Goats adds 2 mana of chosen color and gains 2 life")
    void thirdAbilitySacrificesGoatsForManaAndLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        Permanent goat1 = addGoat(player1);
        Permanent goat2 = addGoat(player1);
        int lifeBefore = gd.getLife(player1.getId());

        int idx = indexOf(pasture);
        // X=2: exactly two Goats available -> both auto-sacrificed
        harness.activateAbility(player1, idx, 2, 2, null);

        // Mana ability -> prompts color choice, does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.stack).isEmpty();
        // Both Goats sacrificed as cost; life already gained during resolution
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat1, goat2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability chooses which Goats to sacrifice when more than X are available")
    void thirdAbilityInteractiveGoatChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        Permanent goat1 = addGoat(player1);
        Permanent goat2 = addGoat(player1);
        Permanent goat3 = addGoat(player1);

        int idx = indexOf(pasture);
        // X=2 with 3 Goats -> player chooses which two
        harness.activateAbility(player1, idx, 2, 2, null);
        harness.handlePermanentChosen(player1, goat1.getId());
        harness.handlePermanentChosen(player1, goat2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat1, goat2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goat3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Third ability fails when fewer Goats than X are available")
    void thirdAbilityFailsWithoutEnoughGoats() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        addGoat(player1);

        int idx = indexOf(pasture);
        // X=2 but only one Goat
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 2, 2, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addPasture() {
        harness.addToBattlefield(player1, new SpringjackPasture());
        return findPermanent(player1, "Springjack Pasture");
    }

    private Permanent addGoat(Player player) {
        Card goat = new GrizzlyBears();
        goat.setSubtypes(List.of(CardSubtype.GOAT));
        Permanent perm = new Permanent(goat);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
