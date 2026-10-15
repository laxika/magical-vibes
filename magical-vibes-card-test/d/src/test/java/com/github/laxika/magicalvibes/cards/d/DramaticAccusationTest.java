package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.ForumFamiliar;
import com.github.laxika.magicalvibes.cards.u.UnauthorizedExit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DramaticAccusation.class, ForumFamiliar.class, UnauthorizedExit.class})
class DramaticAccusationTest extends BaseCardTest {

    @Test
    @DisplayName("When Dramatic Accusation enters, it taps the enchanted creature")
    void entersAndTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new ForumFamiliar());

        harness.setHand(player1, List.of(new DramaticAccusation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap while Dramatic Accusation remains attached")
    void enchantedCreatureDoesNotUntapUntilAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new ForumFamiliar());
        creature.tap();

        Permanent aura = new Permanent(new DramaticAccusation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating Dramatic Accusation shuffles the enchanted creature into its owner's library")
    void activatingAbilityShufflesEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new ForumFamiliar());

        Permanent aura = new Permanent(new DramaticAccusation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forum Familiar"));
    }

    @Test
    @DisplayName("Activated ability still shuffles the creature after the Aura leaves")
    void activatedAbilityResolvesAfterAuraIsReturnedToHand() {
        Permanent creature = addCreatureReady(player2, new ForumFamiliar());
        Permanent aura = new Permanent(new DramaticAccusation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.assertInHand(player1, "Dramatic Accusation");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forum Familiar");
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Shuffle uses the creature's owner rather than its controller")
    void shufflesStolenCreatureIntoOwnersLibrary() {
        Permanent creature = addCreatureReady(player1, new ForumFamiliar());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        Permanent aura = new Permanent(new DramaticAccusation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forum Familiar");
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature.getCard());
        harness.assertInGraveyard(player1, "Dramatic Accusation");
    }
}
