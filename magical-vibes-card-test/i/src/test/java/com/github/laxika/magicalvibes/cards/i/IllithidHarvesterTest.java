package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlantTadpoles;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllithidHarvester.class, PlantTadpoles.class, GrizzlyBears.class, DelverOfSecrets.class})
class IllithidHarvesterTest extends BaseCardTest {

    @Test
    void entersAndTurnsTappedNontokenCreaturesFaceDownAsHorrors() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.tap();
        second.tap();

        harness.setHand(player1, List.of(new IllithidHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.isFaceDown()).isTrue();
        assertThat(second.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).containsExactly(CardSubtype.HORROR);
    }

    @Test
    void cannotTargetAnUntappedCreatureWithCeremorphosis() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IllithidHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped nontoken creatures");
    }

    @Test
    void adventureTapsExactlyXCreaturesAndTheySkipTheirNextUntapStep() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        IllithidHarvester card = new IllithidHarvester();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playAdventureCard(gd, player1, 0, 2, null, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        IllithidHarvester card = new IllithidHarvester();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, 0, java.util.Map.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, card.getId(), target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Illithid Harvester");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void ceremorphosisCannotTurnDoubleFacedCreaturesFaceDown() {
        Permanent target = addCreatureReady(player2, new DelverOfSecrets());
        target.tap();
        harness.setHand(player1, List.of(new IllithidHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void ceremorphosisLeavesTargetThatUntapsBeforeResolutionFaceUp() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new IllithidHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        first.untap();
        resolveAllTriggers();

        assertThat(first.isFaceDown()).isFalse();
        assertThat(second.isFaceDown()).isTrue();
    }

    @Test
    void ceremorphosisCanChooseNoTargetsEvenWhenTappedCreaturesExist() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new IllithidHarvester()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Illithid Harvester");
        assertThat(target.isFaceDown()).isFalse();
    }
}
