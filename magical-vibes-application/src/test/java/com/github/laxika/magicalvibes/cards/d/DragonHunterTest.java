package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonHunter.class})
class DragonHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a flying Dragon as though it had reach")
    void canBlockFlyingDragon() {
        Permanent dragonHunter = addReadyPermanent(player2, new DragonHunter(), false);
        Permanent dragon = addReadyPermanent(player1, createCreature("Dragon", CardSubtype.DRAGON, true), true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragonHunter), indexOf(player1, dragon))));

        assertThat(dragonHunter.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block a flying non-Dragon without reach")
    void cannotBlockFlyingNonDragon() {
        Permanent dragonHunter = addReadyPermanent(player2, new DragonHunter(), false);
        Permanent flyer = addReadyPermanent(player1, createCreature("Angel", null, true), true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragonHunter), indexOf(player1, flyer)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Protection from Dragons prevents a Dragon from blocking it")
    void protectionFromDragonsPreventsBlocking() {
        Permanent dragonHunter = addReadyPermanent(player1, new DragonHunter(), true);
        Permanent dragon = addReadyPermanent(player2, createCreature("Dragon", CardSubtype.DRAGON, false), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, dragon), indexOf(player1, dragonHunter)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private static Card createCreature(String name, CardSubtype subtype, boolean flying) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(3);
        card.setToughness(3);
        if (subtype != null) {
            card.setSubtypes(List.of(subtype));
        }
        if (flying) {
            card.setKeywords(Set.of(Keyword.FLYING));
        }
        return card;
    }

    private Permanent addReadyPermanent(Player player, Card card, boolean attacking) {
        card.setOwnerId(player.getId());
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
