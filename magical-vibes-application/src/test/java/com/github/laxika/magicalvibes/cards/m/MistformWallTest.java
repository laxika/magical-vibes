package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MistformWall.class)
class MistformWallTest extends BaseCardTest {

    @Test
    @DisplayName("A Mistform Wall has defender while it is a Wall")
    void hasDefenderAsWall() {
        Permanent wall = addReadyWall();

        assertThat(gqs.effectiveCreatureSubtypes(gd, wall))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Changing its creature type removes defender while the new type lasts")
    void changingTypeRemovesDefender() {
        Permanent wall = addReadyWall();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        assertThat(gqs.effectiveCreatureSubtypes(gd, wall)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Choosing Wall keeps defender until end of turn")
    void choosingWallKeepsDefender() {
        Permanent wall = addReadyWall();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.WALL);

        assertThat(gqs.effectiveCreatureSubtypes(gd, wall)).containsExactly(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("The chosen type and defender condition wear off at end of turn")
    void chosenTypeWearsOff() {
        Permanent wall = addReadyWall();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndChoose(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, wall))
                .containsExactlyInAnyOrder(CardSubtype.ILLUSION, CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    private Permanent addReadyWall() {
        Permanent wall = new Permanent(new MistformWall());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        return wall;
    }

    private void activateAndChoose(CardSubtype subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
    }
}
