package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootcastApprenticeship.class, GrizzlyBears.class, Spellbook.class})
class RootcastApprenticeshipTest extends BaseCardTest {

    @Test
    void resolvesCounterCopyAndTargetPlayerTokenModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent token = addToken(player1, "Test Token", 2, 3);

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), token.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(Permanent::isToken)
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(Permanent::isToken)
                .hasSize(1);
    }

    @Test
    void opponentSacrificesOnlyANontokenArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent artifactToken = addArtifactToken(player2);

        cast(new int[]{3, 3, 3}, List.of(player2.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifactToken);
    }

    @Test
    void sacrificeModeRejectsTargetingTheController() {
        assertThatThrownBy(() -> cast(new int[]{3, 3, 3},
                List.of(player1.getId(), player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RootcastApprenticeship()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(4, modeIndices), targetIds);
    }

    private Permanent addToken(Player player, String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addArtifactToken(Player player) {
        Card card = new Card();
        card.setName("Artifact Token");
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
