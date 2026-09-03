package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({KeenEyedCurator.class, Forest.class, GrizzlyBears.class, Shock.class, TormodsCrypt.class})
class KeenEyedCuratorTest extends BaseCardTest {

    @Test
    @DisplayName("It is a 3/3 without four card types exiled with it")
    void noBonusBeforeFourExiledCardTypes() {
        Permanent curator = addCurator();

        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Exiling four card types gives it +4/+4 and trample")
    void gainsBonusAfterExilingFourCardTypes() {
        Permanent curator = addCurator();
        List<com.github.laxika.magicalvibes.model.Card> cards = List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new TormodsCrypt());
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (var card : cards) {
            harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.exiledCards).allMatch(entry -> curator.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Its ability cannot target a permanent instead of a graveyard card")
    void cannotTargetPermanent() {
        addCurator();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCurator() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new KeenEyedCurator());
        curator.setSummoningSick(false);
        return curator;
    }
}
