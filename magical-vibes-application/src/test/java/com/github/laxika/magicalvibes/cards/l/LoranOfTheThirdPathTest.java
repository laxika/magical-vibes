package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoranOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("When Loran enters, it destroys an artifact")
    void entersAndDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        castLoran(List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("When Loran enters, it destroys an enchantment")
    void entersAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        castLoran(List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Loran's enter-the-battlefield ability may choose no target")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        addLoranMana();

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loran of the Third Path");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Loran's ability makes each player draw a card")
    void eachPlayerDraws() {
        Permanent loran = addReadyLoran(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        setDeck(player1, List.of(new GrizzlyBears()));
        setDeck(player2, List.of(new FountainOfYouth()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(loran.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth");
    }

    @Test
    @DisplayName("Loran's ability cannot target its controller")
    void abilityCannotTargetController() {
        addReadyLoran(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castLoran(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        addLoranMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyLoran(Player player) {
        Permanent loran = new Permanent(new LoranOfTheThirdPath());
        loran.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(loran);
        return loran;
    }

    private void addLoranMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
