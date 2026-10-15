package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CorrosiveOoze;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FungalInfection.class, CorrosiveOoze.class, LlanowarElves.class})
class FungalInfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fungal Infection gives -1/-1 and creates a Saproling token")
    void resolvesDebuffAndCreatesToken() {
        harness.addToBattlefield(player2, new CorrosiveOoze());
        harness.setHand(player1, List.of(new FungalInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Corrosive Ooze");
        harness.castAndResolveInstant(player1, 0, bearId);

        // Target creature gets -1/-1
        Permanent bear = findPermanent(player2, "Corrosive Ooze");
        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);

        // Saproling token created under caster's control
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Target creature with 1 toughness dies and Saproling is still created")
    void killsOneThoughnessCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FungalInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, elvesId);

        // Llanowar Elves (1/1) should die from -1/-1 (0 toughness -> SBA)
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        // Saproling token still created
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Fizzles if target is removed — no Saproling created")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CorrosiveOoze());
        harness.setHand(player1, List.of(new FungalInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Corrosive Ooze");
        harness.castInstant(player1, 0, bearId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        // No Saproling token should be created when the spell fizzles
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Can target your own creature and creates an unaffected green Saproling")
    void targetsOwnCreatureAndCreatesUnaffectedToken() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new FungalInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Llanowar Elves"));

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Fungal Infection");
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Debuff wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player2, new CorrosiveOoze());
        harness.setHand(player1, List.of(new FungalInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Corrosive Ooze");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Corrosive Ooze");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }
}
