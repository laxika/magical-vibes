package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellkeeperWeird.class, HolyDay.class, Divination.class, GrizzlyBears.class})
class SpellkeeperWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target instant from the graveyard and sacrifices itself")
    void returnsTargetInstantAndSacrificesItself() {
        Card instant = new HolyDay();
        Card creature = new GrizzlyBears();
        Permanent spellkeeper = addReadySpellkeeper();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(spellkeeper), null, instant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellkeeper Weird");
        harness.assertInGraveyard(player1, "Spellkeeper Weird");
        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Returns a target sorcery from the graveyard")
    void returnsTargetSorcery() {
        Card sorcery = new Divination();
        Permanent spellkeeper = addReadySpellkeeper();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(spellkeeper), null, sorcery.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sorcery);
    }

    @Test
    @DisplayName("Cannot target a non-instant or non-sorcery card")
    void cannotTargetNonSpellCard() {
        Card creature = new GrizzlyBears();
        Permanent spellkeeper = addReadySpellkeeper();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(spellkeeper), null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySpellkeeper() {
        Permanent spellkeeper = new Permanent(new SpellkeeperWeird());
        spellkeeper.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spellkeeper);
        return spellkeeper;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
