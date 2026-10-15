package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.b.BasilicaSkullbomb;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MandibleJusticiar.class, GlazeFiend.class, BasilicaSkullbomb.class, CrawlingChorus.class})
class MandibleJusticiarTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact you control entering gives Mandible Justiciar +1/+1")
    void allyArtifactEnterBoosts() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(justiciar.getPowerModifier()).isEqualTo(1);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(justiciar.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(justiciar.getPowerModifier()).isEqualTo(0);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Mandible Justiciar")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(justiciar.getPowerModifier()).isEqualTo(0);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mandible Justiciar does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new MandibleJusticiar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent justiciar = findPermanent(player1, "Mandible Justiciar");
        assertThat(justiciar.getPowerModifier()).isZero();
        assertThat(justiciar.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each other artifact entering adds a separate boost")
    void multipleEntriesStackBoosts() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());

        for (int entry = 0; entry < 2; entry++) {
            harness.setHand(player1, List.of(new MandibleJusticiar()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }

        assertThat(justiciar.getPowerModifier()).isEqualTo(2);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lifelink gains life equal to boosted combat damage")
    void lifelinkUsesBoostedPower() {
        addCreatureReady(player1, new MandibleJusticiar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MandibleJusticiar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A noncreature artifact also triggers the boost")
    void noncreatureArtifactTriggers() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());
        harness.setHand(player1, List.of(new BasilicaSkullbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(justiciar.getPowerModifier()).isEqualTo(1);
        assertThat(justiciar.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonartifact creature does not trigger the boost")
    void nonartifactCreatureDoesNotTrigger() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new MandibleJusticiar());
        harness.setHand(player1, List.of(new CrawlingChorus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(justiciar.getPowerModifier()).isZero();
        assertThat(justiciar.getToughnessModifier()).isZero();
    }
}
