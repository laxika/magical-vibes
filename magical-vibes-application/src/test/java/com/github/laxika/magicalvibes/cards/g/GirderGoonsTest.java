package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BodyDropper;
import com.github.laxika.magicalvibes.cards.c.CorruptCourtOfficial;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GirderGoons.class, CorruptCourtOfficial.class, BodyDropper.class})
class GirderGoonsTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast creates a tapped Rogue on death without drawing")
    void normalCastCreatesTokenWithoutBlitzDraw() {
        harness.addToBattlefield(player1, new BodyDropper());
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent goons = findPermanent(player1, "Girder Goons");
        assertThat(gqs.hasKeyword(gd, goons, Keyword.HASTE)).isFalse();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, goons.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Girder Goons");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent rogue = findPermanents(player1, "Rogue").getFirst();
        assertThat(rogue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent goons = findPermanent(player1, "Girder Goons");
        assertThat(gqs.hasKeyword(gd, goons, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goons);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Girder Goons");
        harness.assertInHand(player1, "Corrupt Court Official");
        Permanent rogue = findPermanents(player1, "Rogue").getFirst();
        assertThat(rogue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blitz grants haste immediately as the creature spell resolves")
    void blitzHasHasteBeforeAnyEtbAbilityResolves() {
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent goons = findPermanent(player1, "Girder Goons");
        assertThat(gqs.hasKeyword(gd, goons, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A blitzed creature sacrificed immediately draws and creates exactly one tapped Rogue")
    void blitzDeathBeforeEtbResolutionDrawsAndCreatesToken() {
        harness.addToBattlefield(player1, new BodyDropper());
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.setLibrary(player1, List.of(new CorruptCourtOfficial(), new GirderGoons()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent goons = findPermanent(player1, "Girder Goons");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, goons.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Girder Goons");
        harness.assertInHand(player1, "Corrupt Court Official");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanent(player1, "Rogue").isTapped()).isTrue();
        Permanent rogue = findPermanent(player1, "Rogue");
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rogue)).isEqualTo(2);
        assertThat(rogue.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(rogue.getCard().getSubtypes()).containsExactly(CardSubtype.ROGUE);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
    }

    @Test
    @DisplayName("Normally cast Girder Goons is not sacrificed at the end step")
    void normalCastSurvivesEndStep() {
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Girder Goons");
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
