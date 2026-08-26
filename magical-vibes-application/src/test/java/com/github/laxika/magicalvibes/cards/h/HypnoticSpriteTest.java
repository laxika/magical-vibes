package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MesmericGlare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypnoticSprite.class, MesmericGlare.class, CentaurCourser.class, CrawWurm.class, GiantGrowth.class, GrizzlyBears.class})
class HypnoticSpriteTest extends BaseCardTest {

    @Test
    void adventureCountersSpellWithManaValueThreeAndExilesTheCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        CentaurCourser target = new CentaurCourser();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 3);
        HypnoticSprite card = new HypnoticSprite();
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAdventure(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Centaur Courser");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player2.getId());
    }

    @Test
    void adventureCannotTargetSpellWithManaValueFourOrMore() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        CrawWurm target = new CrawWurm();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 6);
        HypnoticSprite card = new HypnoticSprite();
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castAdventure(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GiantGrowth target = new GiantGrowth();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 1);
        HypnoticSprite card = new HypnoticSprite();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hypnotic Sprite");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }
}
