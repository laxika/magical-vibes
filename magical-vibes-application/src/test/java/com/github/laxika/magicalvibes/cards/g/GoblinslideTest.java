package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinslideTest extends BaseCardTest {

    @Test
    void payingCreatesAHastyGoblinTokenForANoncreatureSpell() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void decliningDoesNotCreateAToken() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Goblin"));
    }

    @Test
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Goblin"));
    }
}
