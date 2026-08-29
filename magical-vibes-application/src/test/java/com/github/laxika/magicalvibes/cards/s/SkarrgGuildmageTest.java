package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkarrgGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives trample to creatures you control, including itself")
    void firstAbilityGrantsTrample() {
        Permanent guildmage = addGuildmage(player1);
        Permanent bears = addBears(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("First ability does not give trample to opponent creatures")
    void firstAbilitySkipsOpponentCreatures() {
        addGuildmage(player1);
        Permanent opponentBears = addBears(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Second ability turns target land into a 4/4 Elemental that is still a land")
    void secondAbilityAnimatesLand() {
        addGuildmage(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Land animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        addGuildmage(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, land)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Second ability cannot target a land an opponent controls")
    void cannotAnimateOpponentLand() {
        addGuildmage(player1);
        Permanent opponentLand = addLand(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a nonland creature")
    void cannotAnimateNonLand() {
        addGuildmage(player1);
        Permanent bears = addBears(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addGuildmage(Player player) {
        return addReady(player, new SkarrgGuildmage());
    }

    private Permanent addBears(Player player) {
        return addReady(player, new GrizzlyBears());
    }

    private Permanent addLand(Player player) {
        return addReady(player, new Forest());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
