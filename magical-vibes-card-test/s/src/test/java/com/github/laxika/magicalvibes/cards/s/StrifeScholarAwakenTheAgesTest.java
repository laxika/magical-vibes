package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RapierWit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrifeScholarAwakenTheAges.class, RapierWit.class})
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

        List<Permanent> spirits = findPermanents(player1, "Spirit").stream()
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

    @Test
    void enteringPreparedDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new StrifeScholarAwakenTheAges()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Strife Scholar").isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingUnpreparesBeforeTheSpellResolves() {
        Permanent scholar = castScholar();
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFromExile(player1, scholar.getPreparedSpellCardId());

        assertThat(scholar.isPrepared()).isFalse();
        assertThat(scholar.getPreparedSpellCardId()).isNull();
        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    void unpaidWardCountersOpponentsSpell() {
        Permanent scholar = castScholar();
        castRapierWitAtScholar(scholar);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(scholar.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Rapier Wit");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoLifeAllowsOpponentsSpellToResolve() {
        Permanent scholar = castScholar();
        castRapierWitAtScholar(scholar);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(scholar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotUnprepareScholar() {
        Permanent scholar = castScholar();
        UUID copyId = scholar.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scholar.isPrepared()).isTrue();
        assertThat(scholar.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }
    private void castRapierWitAtScholar(Permanent scholar) {
        harness.setLibrary(player2, List.of(new StrifeScholarAwakenTheAges()));
        harness.setHand(player2, List.of(new RapierWit()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, scholar.getId());
    }

    private Permanent castScholar() {
        harness.setHand(player1, List.of(new StrifeScholarAwakenTheAges()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Strife Scholar");
    }
}
