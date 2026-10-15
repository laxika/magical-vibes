package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurveyTheWreckage.class, Mountain.class, AxebaneStag.class})
class SurveyTheWreckageTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new SurveyTheWreckage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Destroys the target land and creates a 1/1 Goblin for the caster")
    void destroysLandAndCreatesGoblin() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepare();

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(1);
        assertThat(goblins.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(goblins.getFirst().getEffectiveToughness()).isEqualTo(1);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Whole spell fizzles (no Goblin) when its only target is gone")
    void fizzlesWhenTargetGone() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepare();

        harness.castSorcery(player1, 0, land.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxebaneStag());
        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy the caster's own land and creates a red Goblin token")
    void canTargetOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        prepare();

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().isToken()).isTrue();
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(goblin.isSummoningSick()).isTrue();
        assertThat(goblin.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Survey the Wreckage");
    }

    @Test
    @DisplayName("Creates a Goblin even when regeneration prevents the land's destruction")
    void createsGoblinWhenLandRegenerates() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.setRegenerationShield(1);
        prepare();

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        assertThat(land.getRegenerationShield()).isZero();
        assertThat(land.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }
}
