package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrickstersTalisman.class, GrizzlyBears.class})
class TrickstersTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger sacrifices the Talisman and creates a creature copy")
    void acceptingTriggerSacrificesTalismanAndCreatesCopy() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Trickster's Talisman");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the Talisman attached")
    void decliningTriggerLeavesTalismanAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = attachTalisman(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(talisman);
        assertThat(talisman.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent attachTalisman(Player player, Permanent creature) {
        Permanent talisman = new Permanent(new TrickstersTalisman());
        talisman.setSummoningSick(false);
        talisman.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(talisman);
        return talisman;
    }
}
