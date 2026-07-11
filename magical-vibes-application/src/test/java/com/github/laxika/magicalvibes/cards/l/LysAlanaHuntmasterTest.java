package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LysAlanaHuntmasterTest extends BaseCardTest {

    private void giveElfSpell(Player caster) {
        // Elvish Eulogist is a {G} Elf Shaman (not a Warrior, so it never inflates the token count).
        harness.setHand(caster, List.of(new ElvishEulogist()));
        harness.addMana(caster, ManaColor.GREEN, 1);
    }

    private long elfWarriorTokens(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .count();
    }

    @Test
    @DisplayName("Casting an Elf spell offers the controller a may ability")
    void elfSpellTriggers() {
        harness.addToBattlefield(player1, new LysAlanaHuntmaster());
        giveElfSpell(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting creates a 1/1 green Elf Warrior token")
    void acceptCreatesToken() {
        harness.addToBattlefield(player1, new LysAlanaHuntmaster());
        giveElfSpell(player1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(1)
                .allMatch(p -> p.getCard().getPower() == 1 && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.ELF)
                        && p.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    @Test
    @DisplayName("Declining creates no token")
    void declineCreatesNoToken() {
        harness.addToBattlefield(player1, new LysAlanaHuntmaster());
        giveElfSpell(player1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(elfWarriorTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Casting a non-Elf spell does not trigger the ability")
    void nonElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LysAlanaHuntmaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an Elf spell does not trigger the controller's ability")
    void opponentElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LysAlanaHuntmaster());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveElfSpell(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
