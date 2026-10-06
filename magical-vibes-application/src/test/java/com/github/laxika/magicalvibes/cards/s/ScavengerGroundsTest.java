package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.i.IfnirDeadlands;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengerGrounds.class, Abrade.class, FeralProwler.class, IfnirDeadlands.class})
class ScavengerGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyGrounds(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{2}, {T}, Sacrifice a Desert: exiles all graveyards")
    void exileAbilityClearsAllGraveyards() {
        Permanent grounds = addReadyGrounds(player1);
        harness.setGraveyard(player1, List.of(new Abrade()));
        harness.setGraveyard(player2, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Abrade"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Feral Prowler"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(grounds.getId()));
        // Sacrificed as cost into GY, then exiled when the ability resolves.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scavenger Grounds"));
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent otherDesert = harness.addToBattlefieldAndReturn(player1, new IfnirDeadlands());
        otherDesert.setSummoningSick(false);
        harness.setGraveyard(player2, List.of(new FeralProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(grounds.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, and cards arriving later are also exiled")
    void paysSacrificeBeforeExilingCurrentGraveyards() {
        Permanent grounds = addReadyGrounds(player1);
        Abrade initialCard = new Abrade();
        FeralProwler laterCard = new FeralProwler();
        harness.setGraveyard(player1, List.of(initialCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grounds);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(initialCard, grounds.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player2.getId()).add(laterCard);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(initialCard, grounds.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(laterCard);
    }

    @Test
    @DisplayName("An already tapped Desert can pay the sacrifice cost with initially empty graveyards")
    void sacrificesTappedDesertWithEmptyGraveyards() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new IfnirDeadlands());
        desert.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, desert.getId());

        assertThat(grounds.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(desert.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(grounds);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(desert.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling requires two mana in addition to tapping and sacrificing")
    void cannotActivateWithoutEnoughMana() {
        Permanent grounds = addReadyGrounds(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(grounds);
        assertThat(grounds.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyGrounds(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ScavengerGrounds());
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
