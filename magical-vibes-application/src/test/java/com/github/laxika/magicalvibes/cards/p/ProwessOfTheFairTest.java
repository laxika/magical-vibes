package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LysAlanaScarblade;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Wispmare;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwessOfTheFair.class, LysAlanaScarblade.class, GrizzlyBears.class, Shock.class, Wispmare.class})
class ProwessOfTheFairTest extends BaseCardTest {

    // "Whenever another nontoken Elf is put into your graveyard from the battlefield,
    //  you may create a 1/1 green Elf Warrior creature token."

    private List<Permanent> elfWarriorTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .toList();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities(); // resolve the death trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("Accepting the may ability creates a 1/1 green Elf Warrior when a nontoken Elf dies")
    void acceptingCreatesTokenWhenElfDies() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player1, new LysAlanaScarblade()); // nontoken Elf

        killWithShock(player1, player1, "Lys Alana Scarblade");
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> tokens = elfWarriorTokens(player1);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Declining the may ability creates no token")
    void decliningCreatesNoToken() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player1, new LysAlanaScarblade());

        killWithShock(player1, player1, "Lys Alana Scarblade");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elfWarriorTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A non-Elf creature dying does not trigger")
    void nonElfDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 Bear, not an Elf

        killWithShock(player1, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(elfWarriorTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An Elf token dying does not trigger (nontoken only)")
    void elfTokenDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player1, new LysAlanaScarblade());

        // Kill the nontoken Elf to produce an Elf Warrior token.
        killWithShock(player1, player1, "Lys Alana Scarblade");
        harness.handleMayAbilityChosen(player1, true);
        assertThat(elfWarriorTokens(player1)).hasSize(1);

        // Now kill the token itself: the nontoken slot must not fire again.
        killWithShock(player1, player1, "Elf Warrior");

        assertThat(gd.stack).isEmpty();
        assertThat(elfWarriorTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Elf dying does not trigger")
    void opponentElfDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player2, new LysAlanaScarblade());

        killWithShock(player1, player2, "Lys Alana Scarblade");

        assertThat(gd.stack).isEmpty();
        assertThat(elfWarriorTokens(player1)).isEmpty();
    }

    @Test
    void anotherNoncreatureElfGoingToGraveyardTriggers() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ProwessOfTheFair());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, other.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prowess of the Fair");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(elfWarriorTokens(player1)).hasSize(1);
    }

    @Test
    void ownedElfDyingUnderOpponentControlTriggers() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new LysAlanaScarblade());
        gd.stolenCreatures.put(elf.getId(), player1.getId());

        killWithShock(player1, player2, "Lys Alana Scarblade");

        harness.assertInGraveyard(player1, "Lys Alana Scarblade");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(elfWarriorTokens(player1)).hasSize(1);
    }

    @Test
    void opponentOwnedElfDyingUnderYourControlDoesNotTrigger() {
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LysAlanaScarblade());
        gd.stolenCreatures.put(elf.getId(), player2.getId());

        killWithShock(player1, player1, "Lys Alana Scarblade");

        harness.assertInGraveyard(player2, "Lys Alana Scarblade");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(elfWarriorTokens(player1)).isEmpty();
    }
}
