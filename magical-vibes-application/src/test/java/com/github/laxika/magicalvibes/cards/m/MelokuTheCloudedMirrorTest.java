package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MelokuTheCloudedMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and creates a 1/1 flying Illusion token")
    void returnsLandAndCreatesToken() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        harness.activateAbility(player1, melokuIndex, null, null);

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Illusion"))
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        assertThatThrownBy(() -> harness.activateAbility(player1, melokuIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAvailable() {
        harness.addToBattlefield(player1, new MelokuTheCloudedMirror());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent plains = findPermanent(player1, "Plains");

        int melokuIndex = battlefieldIndex(player1, "Meloku the Clouded Mirror");
        harness.activateAbility(player1, melokuIndex, null, null);

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, plains.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
