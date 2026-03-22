package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.CardSubtype;
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
        CardView cardView = grantedSubtypes.isEmpty()
                ? cardViewFactory.create(entry.getCard())
                : cardViewFactory.create(entry.getCard(), grantedSubtypes);
        return new StackEntryView(
                entry.getEntryType(),
                cardView,
                entry.getControllerId(),
                entry.getDescription(),
                entry.getCard().getId(),
                SPELL_TYPES.contains(entry.getEntryType()),
                entry.getTargetId()
        );
    }
}
