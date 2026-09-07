package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PacificationArray;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwashbucklersWhip.class, GrizzlyBears.class, PacificationArray.class, Pacifism.class})
class SwashbucklersWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has reach")
    void equippedCreatureHasReach() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip {1} attaches the Whip to a creature you control")
    void equipAttachesToCreature() {
        Permanent whip = addWhipReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap a target artifact or creature")
    void equippedCreatureCanTapArtifactOrCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = addPermanent(player2, new PacificationArray());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature cannot target an enchantment")
    void equippedCreatureCannotTargetEnchantment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Permanent target = addPermanent(player2, new Pacifism());

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Equipped creature can discover 10")
    void equippedCreatureCanDiscoverTen() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addWhipReady(player1);
        whip.setAttachedTo(creature.getId());
        Card discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    private Permanent addWhipReady(Player player) {
        return addPermanent(player, new SwashbucklersWhip());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
