package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoneToAsh;
import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenHeretic.class, SanctuaryCat.class, BoneToAsh.class})
class ThrabenHereticTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creature card from controller's graveyard")
    void exilesCreatureFromOwnGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card creature = new SanctuaryCat();
        harness.setGraveyard(player1, List.of(creature));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Sanctuary Cat");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sanctuary Cat"));
    }

    @Test
    @DisplayName("Can exile creature card from opponent's graveyard")
    void exilesCreatureFromOpponentGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card creature = new SanctuaryCat();
        harness.setGraveyard(player2, List.of(creature));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Sanctuary Cat");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Sanctuary Cat"));
    }

    @Test
    @DisplayName("Rejects non-creature card as target")
    void rejectsNonCreatureTarget() {
        Permanent heretic = addReadyHeretic(player1);
        Card instant = new BoneToAsh();
        harness.setGraveyard(player1, List.of(instant));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent heretic = addReadyHeretic(player1);
        Card creature = new SanctuaryCat();

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Fizzles if target removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent heretic = addReadyHeretic(player1);
        Card creature = new SanctuaryCat();
        harness.setGraveyard(player1, List.of(creature));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Sanctuary Cat"));
    }

    @Test
    @DisplayName("Activating ability taps Thraben Heretic")
    void activatingTapsHeretic() {
        Permanent heretic = addReadyHeretic(player1);
        Card creature = new SanctuaryCat();
        harness.setGraveyard(player1, List.of(creature));

        assertThat(heretic.isTapped()).isFalse();

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD);

        assertThat(heretic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent heretic = addReadyHeretic(player1);
        heretic.tap();
        Card creature = new SanctuaryCat();
        harness.setGraveyard(player1, List.of(creature));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent heretic = harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        heretic.setSummoningSick(true);

        Card creature = new SanctuaryCat();
        harness.setGraveyard(player1, List.of(creature));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        Permanent heretic = addReadyHeretic(player1);
        harness.setGraveyard(player2, List.of(new SanctuaryCat()));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);

        assertThatThrownBy(() -> harness.activateAbility(player1, hereticIndex, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heretic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exiles only the chosen creature card")
    void exilesOnlyChosenCreature() {
        Permanent heretic = addReadyHeretic(player1);
        Card target = new SanctuaryCat();
        Card other = new SanctuaryCat();
        Card instant = new BoneToAsh();
        harness.setGraveyard(player2, List.of(target, other, instant));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other, instant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent heretic = addReadyHeretic(player1);
        Card target = new SanctuaryCat();
        harness.setGraveyard(player2, List.of(target));

        int hereticIndex = gd.playerBattlefields.get(player1.getId()).indexOf(heretic);
        harness.activateAbility(player1, hereticIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(heretic);
        harness.setGraveyard(player1, List.of(heretic.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Thraben Heretic");
    }

    private Permanent addReadyHeretic(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThrabenHeretic());
        perm.setSummoningSick(false);
        return perm;
    }
}
