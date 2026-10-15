package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TormentedHero.class, GiantGrowth.class, Shock.class})
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
        harness.castAndResolveInstant(player1, 0, heroId);
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

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent's spell targeting Tormented Hero does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TormentedHero());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Tormented Hero"));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Heroic resolves before the spell targeting Tormented Hero")
    void heroicResolvesBeforeTargetingSpell() {
        harness.addToBattlefield(player1, new TormentedHero());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Tormented Hero"));
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Tormented Hero");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tormented Hero");
        harness.assertNotOnBattlefield(player1, "Tormented Hero");
    }

    @Test
    @DisplayName("Heroic still drains after Tormented Hero leaves the battlefield")
    void heroicResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new TormentedHero());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        UUID heroId = harness.getPermanentId(player1, "Tormented Hero");

        harness.castInstant(player1, 0, heroId);
        harness.castAndResolveInstant(player2, 0, heroId);
        harness.assertInGraveyard(player1, "Tormented Hero");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
