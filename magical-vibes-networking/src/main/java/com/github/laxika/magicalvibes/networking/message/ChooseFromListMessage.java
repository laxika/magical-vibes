package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

/**
 * A "choose one value from a list" prompt. {@code searchable} tells the client to render an
 * autocomplete search box instead of a button-per-option grid — used when the options are a
 * potentially large set of card names (e.g. "choose a nonland creature name") rather than a
 * small fixed set of colors / types.
 */
public record ChooseFromListMessage(MessageType type, List<String> options, String prompt, boolean searchable) {

    public ChooseFromListMessage(List<String> options, String prompt) {
        this(options, prompt, false);
    }

    public ChooseFromListMessage(List<String> options, String prompt, boolean searchable) {
        this(MessageType.CHOOSE_FROM_LIST, options, prompt, searchable);
    }
}
