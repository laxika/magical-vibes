package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelersCloak.class, AncientKavu.class, Forest.class, Swamp.class, Desert.class})
class TravelersCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a land type as Traveler's Cloak enters draws a card")
    void choosingLandTypeDrawsCard() {
        Permanent creature = addCreatureReady(player1, new AncientKavu());
        harness.setHand(player1, List.of(new TravelersCloak()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");
        harness.passBothPriorities();

        Permanent cloak = findPermanent(player1, "Traveler's Cloak");
        assertThat(cloak.getChosenSubtype()).isEqualTo(CardSubtype.SWAMP);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Traveler's Cloak can choose a nonbasic land type for landwalk")
    void choosingNonBasicLandTypeEnablesLandwalk() {
        Permanent attacker = addCreatureReady(player1, new AncientKavu());
        harness.setHand(player1, List.of(new TravelersCloak()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DESERT");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Traveler's Cloak").getChosenSubtype())
                .isEqualTo(CardSubtype.DESERT);

        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AncientKavu());
        harness.addToBattlefield(player2, new Desert());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Enchanted creature cannot be blocked when the defender controls the chosen land type")
    void chosenLandwalkPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new AncientKavu());
        attacker.setAttacking(true);
        attachCloak(attacker, CardSubtype.SWAMP);
        Permanent blocker = addCreatureReady(player2, new AncientKavu());
        harness.addToBattlefield(player2, new Swamp());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked when the defender controls a different land type")
    void differentLandTypeAllowsBlocking() {
        Permanent attacker = addCreatureReady(player1, new AncientKavu());
        attacker.setAttacking(true);
        attachCloak(attacker, CardSubtype.SWAMP);
        Permanent blocker = addCreatureReady(player2, new AncientKavu());
        harness.addToBattlefield(player2, new Forest());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void attachCloak(Permanent creature, CardSubtype chosenSubtype) {
        Permanent cloak = new Permanent(new TravelersCloak());
        cloak.setAttachedTo(creature.getId());
        cloak.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(cloak);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
