package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatWall.class, BarbaryApes.class})
class GreatWallTest extends BaseCardTest {

    @Test
    @DisplayName("Plainswalk can be blocked while Great Wall is on the battlefield")
    void plainswalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.PLAINS));
        harness.addToBattlefield(player2, new GreatWall());
        Permanent attacker = addWalker(player1, Keyword.PLAINSWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);
    }

    @Test
    @DisplayName("Great Wall does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new GreatWall());
        Permanent attacker = addWalker(player1, Keyword.FORESTWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addWalker(Player player, Keyword landwalk) {
        Card card = new Card();
        card.setName("Test Walker");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(EnumSet.of(landwalk));
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private Card basicLand(CardSubtype subtype) {
        Card card = new Card();
        card.setName(subtype.getDisplayName());
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
