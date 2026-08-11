package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TormentedHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Tormented Hero enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TormentedHero()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Tormented Hero");
        assertThat(hero.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a spell that targets Tormented Hero drains each opponent")
    void castingSpellThatTargetsHeroDrainsEachOpponent() {
        harness.addToBattlefield(player1, new TormentedHero());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID heroId = harness.getPermanentId(player1, "Tormented Hero");
        harness.castInstant(player1, 0, heroId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Spells that do not target Tormented Hero do not trigger it")
    void spellThatTargetsPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TormentedHero());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
