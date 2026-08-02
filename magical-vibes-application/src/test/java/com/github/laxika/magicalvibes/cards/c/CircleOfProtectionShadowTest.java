package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CircleOfProtectionShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Only creatures with shadow are offered as prevention sources")
    void onlyShadowCreaturesAreValidSources() {
        addReadyCircle(player1);
        Permanent shadowCreature = addReadyShadowCreature(player2);
        Permanent normalCreature = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(shadowCreature.getId())
                .doesNotContain(normalCreature.getId());
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen shadow creature")
    void preventsNextDamageFromChosenShadowCreature() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent shadowCreature = addReadyShadowCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shadowCreature.getId());

        shadowCreature.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        Permanent permanent = new Permanent(new CircleOfProtectionShadow());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyShadowCreature(Player player) {
        Card card = new Card();
        card.setName("Shadow Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.SHADOW));
        return addCreatureReady(player, card);
    }
}
