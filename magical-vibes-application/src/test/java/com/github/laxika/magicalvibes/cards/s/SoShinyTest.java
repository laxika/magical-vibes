package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoShiny.class, GrizzlyBears.class})
class SoShinyTest extends BaseCardTest {

    @Test
    void tokenControlTapsEnchantedCreatureAndScriesTwo() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addToken(player1);
        castSoShiny(creature);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void withoutTokenControlEtbDoesNotTapOrScry() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castSoShiny(creature);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enchantedCreatureDoesNotUntapDuringItsControllersUntapStep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new SoShiny());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    private void castSoShiny(Permanent creature) {
        harness.setHand(player1, List.of(new SoShiny()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
    }

    private void addToken(com.github.laxika.magicalvibes.model.Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player, token);
    }
}
