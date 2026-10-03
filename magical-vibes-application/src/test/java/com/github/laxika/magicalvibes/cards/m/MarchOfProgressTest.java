package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfProgress.class, DarksteelMyr.class, GrizzlyBears.class})
class MarchOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the target artifact creature")
    void copiesTargetArtifactCreature() {
        Permanent target = addArtifactCreature(player1, "Target Myr");
        addArtifactCreature(player1, "Other Myr");
        castNormally(target);

        assertThat(tokenCount(player1, "Target Myr")).isEqualTo(1);
        assertThat(tokenCount(player1, "Other Myr")).isZero();
    }

    @Test
    @DisplayName("Overload copies each artifact creature you control")
    void overloadCopiesEachArtifactCreatureYouControl() {
        addArtifactCreature(player1, "First Myr");
        addArtifactCreature(player1, "Second Myr");
        harness.addToBattlefield(player1, new GrizzlyBears());
        addArtifact(player1, "Equipment");
        addArtifactCreature(player2, "Enemy Myr");

        harness.setHand(player1, List.of(new MarchOfProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(tokenCount(player1, "First Myr")).isEqualTo(1);
        assertThat(tokenCount(player1, "Second Myr")).isEqualTo(1);
        assertThat(tokenCount(player1, "Grizzly Bears")).isZero();
        assertThat(tokenCount(player1, "Equipment")).isZero();
        assertThat(tokenCount(player2, "Enemy Myr")).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new MarchOfProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNormally(Permanent target) {
        harness.setHand(player1, List.of(new MarchOfProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addArtifactCreature(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        card.setPower(2);
        card.setToughness(2);
        return addPermanent(player, card);
    }

    private Permanent addArtifact(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return addPermanent(player, card);
    }

    private Permanent addCreature(Player player) {
        return addPermanent(player, new GrizzlyBears());
    }

    private Permanent addPermanent(Player player, Card card) {
        harness.addToBattlefield(player, card);
        Permanent permanent = gd.playerBattlefields.get(player.getId()).getLast();
        permanent.setSummoningSick(false);
        return permanent;
    }

    private long tokenCount(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }
}
