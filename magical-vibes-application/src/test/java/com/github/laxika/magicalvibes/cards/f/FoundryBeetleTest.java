package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WebspinnerCuff;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoundryBeetle.class, GrizzlyBears.class, WebspinnerCuff.class})
class FoundryBeetleTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFirstStrikeAndBeetleStopsBeingACreature() {
        Permanent beetle = addReadyBeetle();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addRedMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(beetle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        addRedMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(beetle.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, beetle)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void upkeepReducesOneRandomArtifactCardInHand() {
        addReadyBeetle();
        harness.setHand(player1, List.of(new WebspinnerCuff(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Webspinner Cuff"));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBeetle() {
        return addCreatureReady(player1, new FoundryBeetle());
    }

    private void addRedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
