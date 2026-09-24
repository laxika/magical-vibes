package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphantReckoning.class, AjaniGoldmane.class, GloriousAnthem.class, GrizzlyBears.class, Ornithopter.class, GhostlyPrison.class, IchorWellspring.class, JaceBeleren.class})
class TriumphantReckoningTest extends BaseCardTest {

    @Test
    void returnsAllOwnArtifactsEnchantmentsAndPlaneswalkersFromGraveyard() {
        Card artifact = new IchorWellspring();
        Card enchantment = new GhostlyPrison();
        Card planeswalker = new JaceBeleren();
        Card creature = new GrizzlyBears();
        Card opponentArtifact = new IchorWellspring();
        Card reckoning = new TriumphantReckoning();
        harness.setGraveyard(player1, List.of(artifact, enchantment, planeswalker, creature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(reckoning));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artifact, enchantment, planeswalker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, reckoning);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }
}
