package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * The "except ..." of a copy effect ("becomes a copy of it, except it has flying"; "It's still a land"): values the
 * copy has beside those it copied. They become part of the copy's copiable values (CR 707.9b), so a copy of the copy
 * has them too.
 *
 * @param addedCardTypes card types the copy has in addition to the copied ones ("it's still a land")
 * @param addedKeywords  keyword abilities the copy has in addition to the copied ones ("except it has flying")
 */
public record CopyExceptions(Set<CardType> addedCardTypes, Set<Keyword> addedKeywords) {

    /** No exceptions: the copy has exactly the copied values. */
    public static final CopyExceptions NONE = new CopyExceptions(Set.of(), Set.of());

    public CopyExceptions {
        addedCardTypes = Set.copyOf(addedCardTypes);
        addedKeywords = Set.copyOf(addedKeywords);
    }

    /** "Except it has [keywords]." */
    public static CopyExceptions withKeywords(Keyword... keywords) {
        return new CopyExceptions(Set.of(), Set.of(keywords));
    }

    /** "It's still a [type]": the copy keeps that card type in addition to the copied ones. */
    public static CopyExceptions stillA(CardType... types) {
        return new CopyExceptions(Set.of(types), Set.of());
    }
}
