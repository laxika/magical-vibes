package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WitsEnd;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyChariot(Player player) {
        Permanent permanent = new Permanent(new CryptcallerChariot());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
