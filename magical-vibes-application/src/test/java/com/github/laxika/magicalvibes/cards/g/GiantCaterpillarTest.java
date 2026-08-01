package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GiantCaterpillarTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices self and registers delayed Butterfly; no token yet")
    void abilityRegistersDelayedButterfly() {
        setupCaterpillar();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Caterpillar");
        harness.assertInGraveyard(player1, "Giant Caterpillar");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Butterfly");
    }

    @Test
    @DisplayName("Creates a 1/1 green Insect Butterfly with flying at next end step")
    void createsButterflyTokenAtNextEndStep() {
        setupCaterpillar();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Butterfly");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    private void setupCaterpillar() {
        harness.addToBattlefield(player1, new GiantCaterpillar());
        findPermanent(player1, "Giant Caterpillar").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
    }
}
