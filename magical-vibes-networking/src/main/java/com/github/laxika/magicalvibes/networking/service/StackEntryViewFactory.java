package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StackEntryViewFactory {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL,
            StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL,
            StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL,
            StackEntryType.PLANESWALKER_SPELL
    );

    private final CardViewFactory cardViewFactory;

    public StackEntryView create(StackEntry entry) {
        return create(entry, List.of());
    }

    /**
     * Creates a StackEntryView with additional granted subtypes applied to creature spells.
     */
    public StackEntryView create(StackEntry entry, List<CardSubtype> grantedSubtypes) {
        CardView cardView = entry.getCard() == null ? null : grantedSubtypes.isEmpty()
                ? cardViewFactory.create(entry.getCard())
                : cardViewFactory.create(entry.getCard(), grantedSubtypes);
        return createView(entry, cardView, entry.getDescription());
    }

    /** A spell cast face down, as a player who may not look at it sees it (CR 708.2, 708.4). */
    public StackEntryView createFaceDownSpell(StackEntry entry, List<CardSubtype> grantedSubtypes) {
        CardView cardView = cardViewFactory.createFaceDown(entry.getTargetableId(), Set.of(CardType.CREATURE),
                grantedSubtypes, 2, 2, entry.isCastWithDisguise() ? Set.of(Keyword.WARD) : Set.of());
        return createView(entry, cardView, StackEntry.FACE_DOWN_SPELL);
    }

    /**
     * An ability whose source is face down, as a player who may not look at that card sees it.
     *
     * @param source the face-down permanent, shown by its permanent's id as its battlefield view is; or null for a
     *               card that shows no characteristics at all (CR 406.3)
     */
    public StackEntryView createFaceDownAbility(StackEntry entry, Permanent source, String description) {
        CardView cardView = source == null ? null : cardViewFactory.createFaceDown(source.getId(),
                source.getFaceDownCardTypes(), List.of(), source.getFaceDownPower(), source.getFaceDownToughness(),
                source.isFaceDownWard() || source.isCloaked() ? Set.of(Keyword.WARD) : Set.of());
        return createView(entry, cardView, description);
    }

    private StackEntryView createView(StackEntry entry, CardView cardView, String description) {
        return new StackEntryView(
                entry.getEntryType(),
                cardView,
                entry.getControllerId(),
                description,
                entry.getTargetableId(),
                SPELL_TYPES.contains(entry.getEntryType()),
                entry.getTargetId()
        );
    }
}
