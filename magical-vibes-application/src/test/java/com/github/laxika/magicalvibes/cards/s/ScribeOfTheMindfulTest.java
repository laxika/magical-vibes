package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScribeOfTheMindfulTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target instant from your graveyard to hand and sacrifices itself")
    void returnsInstantAndSacrificesSelf() {
        Permanent scribe = addReadyScribe(player1);
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bolt)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(bolt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(bolt.getId()));
        // Scribe paid the sacrifice cost and is now in the graveyard; the instant is not.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scribe of the Mindful"))
                .noneMatch(c -> c.getId().equals(bolt.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Scribe of the Mindful"));
    }

    @Test
    @DisplayName("Returns a target sorcery from your graveyard to hand")
    void returnsSorcery() {
        Permanent scribe = addReadyScribe(player1);
        Card divination = new Divination();
        harness.setGraveyard(player1, new ArrayList<>(List.of(divination)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(divination.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(divination.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-instant/sorcery card (creature) in the graveyard")
    void cannotTargetCreatureCard() {
        Permanent scribe = addReadyScribe(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        // Illegal activation rewinds: nothing sacrificed, card stays in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Scribe of the Mindful"));
    }

    @Test
    @DisplayName("Cannot target an instant in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent scribe = addReadyScribe(player1);
        Card bolt = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bolt)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyScribe(Player player) {
        Permanent perm = new Permanent(new ScribeOfTheMindful());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int scribeIndex(Permanent scribe) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(scribe);
    }
}
