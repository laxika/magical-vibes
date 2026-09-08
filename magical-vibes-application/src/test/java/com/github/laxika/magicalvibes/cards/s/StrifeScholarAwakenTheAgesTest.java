package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StrifeScholarAwakenTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Strife Scholar enters prepared with an Awaken the Ages copy in exile")
    void entersPrepared() {
        Permanent scholar = castScholar();

        assertThat(scholar.isPrepared()).isTrue();
        UUID copyId = scholar.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting Awaken the Ages unprepares Strife Scholar and creates two Spirit tokens")
    void castingPrepareCopyCreatesSpirits() {
        Permanent scholar = castScholar();
        UUID copyId = scholar.getPreparedSpellCardId();

        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allMatch(spirit -> spirit.getCard().getPower() == 2
                && spirit.getCard().getToughness() == 2
                && spirit.getCard().getColor() == CardColor.RED
                && spirit.getCard().getColors().contains(CardColor.WHITE)
                && spirit.getCard().getType() == CardType.CREATURE
                && spirit.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
        assertThat(scholar.isPrepared()).isFalse();
        assertThat(scholar.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    private Permanent castScholar() {
        harness.setHand(player1, List.of(new StrifeScholarAwakenTheAges()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Strife Scholar");
    }
}
