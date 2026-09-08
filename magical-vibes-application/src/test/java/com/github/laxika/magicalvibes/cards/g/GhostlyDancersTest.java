package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostlyDancers.class, DazzlingTheaterPropRoom.class, GloriousAnthem.class})
class GhostlyDancersTest extends BaseCardTest {

    @Test
    void enteringEnchantmentCreatesAThreeOneFlyingSpirit() {
        addDancers();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findSpiritTokens(player1)).hasSize(1);
        assertThat(findSpiritTokens(player1).getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(findSpiritTokens(player1).getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findSpiritTokens(player1).getFirst(), Keyword.FLYING)).isTrue();
    }

    @Test
    void etbCanReturnAnEnchantmentFromTheGraveyard() {
        Permanent dancers = addDancers();
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(enchantment));
        castDancers(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dancers);
    }

    @Test
    void etbCanUnlockAControlledRoomDoor() {
        Permanent room = castRoom();
        Permanent dancers = addDancers();
        castDancers(1);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).hasSize(1);

        harness.handleListChoice(player1, choice.options().getFirst());
        harness.passBothPriorities();

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(findSpiritTokens(player1)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dancers);
    }

    @Test
    void fullyUnlockingAControlledRoomCreatesAThreeOneFlyingSpirit() {
        Permanent room = castRoom();
        addDancers();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findSpiritTokens(player1)).hasSize(1);
        assertThat(gqs.hasKeyword(gd, findSpiritTokens(player1).getFirst(), Keyword.FLYING)).isTrue();
    }

    private Permanent addDancers() {
        return harness.addToBattlefieldAndReturn(player1, new GhostlyDancers());
    }

    private void castDancers(int mode) {
        harness.setHand(player1, List.of(new GhostlyDancers()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private List<Permanent> findSpiritTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
    }
}
