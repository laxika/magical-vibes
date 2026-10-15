package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WitsEnd;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptcallerChariot.class, GrizzlyBears.class, Peek.class, WitsEnd.class})
class CryptcallerChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one tapped Zombie for each card discarded in one event")
    void createsTappedZombiesForDiscardedCards() {
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.setHand(player1, new ArrayList<>(List.of(new WitsEnd(), new GrizzlyBears(), new Peek())));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(2);
        assertThat(zombies).allMatch(permanent -> permanent.isTapped()
                && permanent.getCard().getPower() == 2
                && permanent.getCard().getToughness() == 2
                && permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Crew 2 animates Cryptcaller Chariot and taps the crew")
    void crewAnimatesChariot() {
        Permanent chariot = addReadyChariot(player1);
        Permanent crew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent discarding does not trigger your Chariot")
    void opponentDiscardDoesNotCreateZombies() {
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.setHand(player1, List.of(new WitsEnd()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Discarding an empty hand creates no Zombies")
    void emptyHandDoesNotCreateZombies() {
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.setHand(player1, List.of(new WitsEnd()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Chariot triggers once for a simultaneous discard")
    void multipleChariotsEachCreateTokens() {
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.setHand(player1, List.of(new WitsEnd(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(4).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Each separate discard event uses its own discarded count")
    void separateDiscardEventsUseTheirOwnCounts() {
        harness.addToBattlefield(player1, new CryptcallerChariot());
        harness.setHand(player1, List.of(new WitsEnd(), new Peek()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 14);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);

        harness.setHand(player1, List.of(new WitsEnd(), new GrizzlyBears(), new Peek()));
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(3).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A summoning-sick creature can crew the Chariot")
    void summoningSickCreatureCanCrew() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new CryptcallerChariot());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, chariot)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(chariot.isTapped()).isFalse();
    }

    private Permanent addReadyChariot(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CryptcallerChariot());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
