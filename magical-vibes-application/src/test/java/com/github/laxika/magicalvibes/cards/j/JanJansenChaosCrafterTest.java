package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JanJansenChaosCrafter.class, BraidwoodCup.class, Juggernaut.class})
class JanJansenChaosCrafterTest extends BaseCardTest {

    @Test
    void sacrificesArtifactCreatureForTwoTreasures() {
        addCreatureReady(player1, new JanJansenChaosCrafter());
        harness.addToBattlefield(player1, new Juggernaut());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Juggernaut");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void sacrificesNoncreatureArtifactForTwoConstructs() {
        addCreatureReady(player1, new JanJansenChaosCrafter());
        harness.addToBattlefield(player1, new BraidwoodCup());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Braidwood Cup");
        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(2);
        assertThat(constructs).allSatisfy(construct -> {
            assertThat(construct.getCard().getColors()).isEmpty();
            assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(construct.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
            assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        });
    }

    @Test
    void firstAbilityCannotSacrificeNoncreatureArtifact() {
        addCreatureReady(player1, new JanJansenChaosCrafter());
        harness.addToBattlefield(player1, new BraidwoodCup());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityCannotSacrificeArtifactCreature() {
        addCreatureReady(player1, new JanJansenChaosCrafter());
        harness.addToBattlefield(player1, new Juggernaut());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
