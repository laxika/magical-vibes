package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StormKilnArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact its controller controls")
    void getsPowerForControlledArtifacts() {
        Permanent artist = addReadyCreature(player1, new StormKilnArtist());
        int powerWithoutArtifacts = gqs.getEffectivePower(gd, artist);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, artist)).isEqualTo(powerWithoutArtifacts + 2);
    }

    @Test
    @DisplayName("Casting and copying an instant creates a Treasure for each magecraft trigger")
    void castingAndCopyingInstantCreatesTreasures() {
        addReadyCreature(player1, new StormKilnArtist());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        Permanent conspireA = addReadyCreature(player1, new GrizzlyBears());
        Permanent conspireB = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Treasure")))
                .hasSize(2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
