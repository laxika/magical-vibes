package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattlefieldEntryRequestTest {

    @Test
    void snapshotsCollectionInputs() {
        UUID controllerId = UUID.randomUUID();
        Permanent permanent = new Permanent(new Card());
        Permanent batchMate = new Permanent(new Card());
        Set<CardType> tappedTypes = new HashSet<>(Set.of(CardType.LAND));
        List<Permanent> batch = new ArrayList<>(List.of(batchMate));
        List<String> costs = new ArrayList<>(List.of("{1}{G}"));

        BattlefieldEntryRequest request = new BattlefieldEntryRequest(
                controllerId, permanent, tappedTypes, batch, 3, true, costs);
        tappedTypes.clear();
        batch.clear();
        costs.clear();

        assertThat(request.controllerId()).isEqualTo(controllerId);
        assertThat(request.permanent()).isSameAs(permanent);
        assertThat(request.enterTappedTypes()).containsExactly(CardType.LAND);
        assertThat(request.simultaneouslyEntered()).containsExactly(batchMate);
        assertThat(request.repeatedAdditionalCosts()).containsExactly("{1}{G}");
        assertThat(request.xValue()).isEqualTo(3);
        assertThat(request.kicked()).isTrue();
    }

    @Test
    void exposesUnmodifiableCollections() {
        BattlefieldEntryRequest request = new BattlefieldEntryRequest(
                UUID.randomUUID(), new Permanent(new Card()), Set.of(CardType.CREATURE),
                List.of(), 0, false, List.of());

        assertThatThrownBy(() -> request.enterTappedTypes().add(CardType.LAND))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> request.simultaneouslyEntered().add(new Permanent(new Card())))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> request.repeatedAdditionalCosts().add("{R}"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
